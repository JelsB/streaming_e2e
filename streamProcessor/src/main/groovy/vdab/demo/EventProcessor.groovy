package vdab.demo

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import groovy.transform.CompileStatic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.kstream.Aggregator
import org.apache.kafka.streams.kstream.ForeachAction
import org.apache.kafka.streams.kstream.Initializer
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.KeyValueMapper
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Named
import org.apache.kafka.streams.kstream.Printed
import org.apache.kafka.streams.kstream.ValueJoiner
import org.apache.kafka.streams.kstream.ValueMapper
import org.springframework.stereotype.Component
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology

import java.time.LocalDateTime

@Component
@CompileStatic
class EventProcessor {

    String APPLICATION_ID_CONFIG = "demo-processor"
    String BOOTSTRAP_SERVERS_CONFIG = 'localhost:9092'
    String DEFAULT_KEY_SERDE_CLASS_CONFIG = Serdes.String().getClass().getName()
    String DEFAULT_VALUE_SERDE_CLASS_CONFIG = Serdes.String().getClass().getName()
    String AUTO_OFFSET_RESET_CONFIG = "earliest"
Properties CreateProperties(){
    Properties props= new Properties()

    props.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, this.APPLICATION_ID_CONFIG);
        //props.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, 'kafka01-mlb1abt.ops.vdab.be:9092,kafka02-mlb1abt.ops.vdab.be:9092,kafka03-mlb1abt.ops.vdab.be:9092')
        props.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, this.BOOTSTRAP_SERVERS_CONFIG)
        props.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, this.DEFAULT_KEY_SERDE_CLASS_CONFIG)
        props.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, this.DEFAULT_VALUE_SERDE_CLASS_CONFIG)
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, this.AUTO_OFFSET_RESET_CONFIG)

    props
}
    Gson gson


    static String extractAfterCange(String inputJson)
    {

        JsonElement rootElement= JsonParser.parseString(inputJson)
        return rootElement.getAsJsonObject().getAsJsonObject("payload" ).getAsJsonObject("after")
    }
    StreamsBuilder builder
    void startUp(){
        gson = new GsonBuilder().registerTypeAdapter(LocalDateTime,new LocalDateTimeAdapter()).serializeNulls().create()

        builder   = new StreamsBuilder();
//
        KStream<String,VMASEvent> vmasStream= createVMASStream()
        KTable<String,String> kansOpWerkTable=createKansOpWerkTable()
//
//
        KStream<String,VMAS_KansOpWerk> joined_KOW_VMAS= Join_VMAS_KOW(vmasStream,kansOpWerkTable)
        vmasAggregate_Kleur(joined_KOW_VMAS)
//

        KTable<String,String> klantTable = createKlantTable()


        Aggregate_VMAS_KLEUR_PROVINTIE(joined_KOW_VMAS,klantTable)


        Topology topo = builder.build();
        KafkaStreams streams =new KafkaStreams(topo,CreateProperties());
//        streams.cleanUp()
        streams.start()


    }



    KTable<String,String> createKansOpWerkTable() {
        KStream<String,String> stream= builder.stream( "kansopwerk")

        KStream<String,KansOpWerk> rawStream=stream.mapValues(new ValueMapper<String, KansOpWerk>() {
            @Override
            KansOpWerk apply(String value) {
                return gson.fromJson(value,KansOpWerk.class)
            }
        }).selectKey(new KeyValueMapper<String, KansOpWerk, String>() {
            @Override
            String apply(String key, KansOpWerk value) {
                return value.ikl.toString()
            }
        }
        )

        return rawStream.mapValues(
                new ValueMapper<KansOpWerk, String>() {
                    @Override
                    String apply(KansOpWerk value) {
                        return gson.toJson(value)
                    }
                }
        ).toTable(Named.as("KOW"))

    }


    KTable<String,String> createKlantTable() {
        KStream<String,String> klantStreamRaw= builder.stream("vdp.public.klant")

        KStream<String,Klant> klantStr=klantStreamRaw.mapValues(new ValueMapper<String, Klant>() {
            @Override
            Klant apply(String value) {
                return (Klant) gson.fromJson(extractAfterCange(value), Klant.class )

            }
        }).selectKey(new KeyValueMapper<String, Klant, String>() {
            @Override
            String apply(String key, Klant value) {
                return value.ikl.toString()
            }
        })

             KTable<String,String> klantTabel=klantStr.mapValues(new ValueMapper<Klant, String>() {
            @Override
            String apply(Klant value) {
                println "debug pring -- ${value}"
                return gson.toJson( value)
            }
        }).toTable(Named.as("klant"))

        return klantTabel

    }


    KTable<String,String> createKlantProvincieTable (){

//        KStream<String,String> provincieStreamRaw= builder.stream("vdp.public.provincie")
//
//                KStream<String,Provincie> provincieStr=provincieStreamRaw.mapValues(new ValueMapper<String, Provincie>() {
//            @Override
//            Provincie apply(String value) {
//                return (Provincie) gson.fromJson(extractAfterCange(value), Provincie.class )
//
//            }
//        }).selectKey(new KeyValueMapper<String, Provincie, String>() {
//            @Override
//            String apply(String key, Provincie value) {
//                return value.provincieid.toString()
//            }
//        })
//
//        KTable<String,String> provintieTable = provincieStr.mapValues(new ValueMapper<Provincie, String>() {
//            @Override
//            String apply(Provincie value) {
//                return gson.toJson(value)
//            }
//        }).toTable( Materialized.with(Serdes.String(),Serdes.String()))
//
//
//        ///// KLANT ////
//        KStream<String,String> klantStreamRaw=builder.stream("vdp.public.klant")
//        KStream<String,String> klantStr= klantStreamRaw.mapValues(new ValueMapper<String, Klant>() {
//            @Override
//            Klant apply(String value) {
//                return (Klant) gson.fromJson(extractAfterCange(value), Klant.class )
//            }
//        }) .selectKey(new KeyValueMapper<String, Klant, String>() {
//            @Override
//            String apply(String key, Klant value) {
//                return value.provincieid.toString()
//            }
//        }).mapValues(new ValueMapper<Klant, String>() {
//            @Override
//            String apply(Klant value) {
//                return gson.toJson(value)
//            }
//        })
//        klantStr.foreach(new ForeachAction<String, String>() {
//            @Override
//            void apply(String key, String value) {
//println(" *** KLANT INFO - ${key} -- ${value} " )
//            }
//        })
//
/////// JOIN KLANT-PROVINCIE
//        KStream<String,KlantProvincie> klantProvStr= klantStr.join(provintieTable,
//                new ValueJoiner<String, String, KlantProvincie>() {
//            @Override
//            KlantProvincie apply(String value1, String value2) {
//                println("*****  JOINING *****  klant ${value1} --- ${value2}")
//                KlantProvincie klprov=new KlantProvincie(klant: gson.fromJson(value1,Klant.class) ,provincie: gson.fromJson(value2,Provincie.class) )
//                return klprov
//            }
//        } )
//        KStream<String,String> rekeyAndSerialized= klantProvStr.selectKey(new KeyValueMapper<String, KlantProvincie, String>() {
//            @Override
//            String apply(String key, KlantProvincie  value) {
//                return value.klant.ikl.toString()
//            }
//        }).
//                mapValues(new ValueMapper<KlantProvincie, String>() {
//                    @Override
//                    String apply(KlantProvincie value) {
//                        return gson.toJson(value)
//                    }
//                })
//
//        rekeyAndSerialized.foreach(new ForeachAction<String, String>() {
//            @Override
//            void apply(String key, String value) {
//    println(" ****  JOINED KLANT-PROVINCE **** ${key}  ***** ${value} ")
//            }
//        })
//        return rekeyAndSerialized.toTable(Named.as("Klant-Provincie"))


    }

    KStream<String,VMASEvent> createVMASStream(){
         KStream<String,String> stream= builder.stream( "Axon.VMAS.DomainEvents")

         KStream<String,VMASEvent> resultStream= stream.mapValues(new ValueMapper<String, VMASEvent>() {
             @Override
             VMASEvent apply(String value) {
                 gson.fromJson(value,VMASEvent.class)
             }
         }).selectKey(new KeyValueMapper<String, VMASEvent, String>() {
             @Override
             String apply(String key, VMASEvent value) {
                 return value.payload.ikl.toString()
             }
         })

        return resultStream
     }
    KStream<String,VMAS_KansOpWerk> Join_VMAS_KOW(KStream<String,VMASEvent> vmasStream,KTable<String,String> kansOpWerkTable){

        //convert to string before joining
        KStream<String,String> vmasStringStream=vmasStream.mapValues(new ValueMapper<VMASEvent, String>() {
            @Override
            String apply(VMASEvent value) {
                return gson.toJson(value)
            }
        })

         KStream<String,VMAS_KansOpWerk> joined=vmasStringStream.leftJoin(kansOpWerkTable,new ValueJoiner<String, String, VMAS_KansOpWerk>() {
            @Override
            VMAS_KansOpWerk apply(String value1, String value2) {
                new VMAS_KansOpWerk(rawEvent:  gson.fromJson(value1,VMASEvent.class)  ,kansOpWerk: gson.fromJson(value2,KansOpWerk.class)  )
            }
        })

        return joined

    }

    void vmasAggregate_Kleur(KStream<String,VMAS_KansOpWerk> vmasJoin){
        //create aggregated output
        KStream<String, String>  aggregateStream= vmasJoin

                .selectKey(new KeyValueMapper<String, VMAS_KansOpWerk, String>() {
            @Override
            String apply(String key, VMAS_KansOpWerk value) {
               try {
                   return value.kansOpWerk.color
               }
                catch (Exception){
                    println("NO KOW found for key ${key} ---- ${value}")
                }
                return "****"
            }
        })
        .mapValues(new ValueMapper<VMAS_KansOpWerk, String>() {
            @Override
            String apply(VMAS_KansOpWerk value) {
                return gson.toJson(value)
            }
        })
                .groupByKey()
                .aggregate(
                new Initializer<String>() {
                    @Override
                    String apply() {
                        gson.toJson(new AggregateRecord())
                    }
                },
                new Aggregator<String, String, String>() {
                    @Override
                    String apply(String key, String value, String  aggregate) {
                        VMAS_KansOpWerk joined =gson.fromJson(value,VMAS_KansOpWerk.class)
//                        println "AGGREGATE --- ${aggregate}"
                        AggregateRecord aggregateObject=null
                        try {
                            aggregateObject = gson.fromJson(aggregate, AggregateRecord.class)
                        }
                        catch (Exception exception){
                            aggregateObject=new AggregateRecord()
                        }
                        aggregateObject.doAggregation(joined.rawEvent.payload.aantalVacatures())
                        if(joined.kansOpWerk)
                            aggregateObject.kleur=joined.kansOpWerk.color
                        else
                            aggregateObject.kleur="***"

                        return gson.toJson(aggregateObject)
                    }
                }
        )
                .toStream()
        aggregateStream.to("OUT_KleurAantalVerzonden")
    }

    def Aggregate_VMAS_KLEUR_PROVINTIE(KStream<String,VMAS_KansOpWerk> joined_KOW_VMAS,KTable<String,String> klantTable){
        joined_KOW_VMAS.foreach(new ForeachAction<String, VMAS_KansOpWerk>() {
            @Override
            void apply(String key, VMAS_KansOpWerk value) {
println("GOT VALUE KOW+VMAS: ${key} === ${value} ")
            }
        })


        klantTable.toStream().foreach(new ForeachAction<String, String>() {
            @Override
            void apply(String key, String value) {
                println("GOT VALUE KLANT : ${key} === ${value} ")
            }
        })

        KStream<String,String> joined_KOW_VMAS_String =joined_KOW_VMAS.mapValues(new ValueMapper<VMAS_KansOpWerk, String>() {
            @Override
            String apply(VMAS_KansOpWerk value) {
                return gson.toJson(value)
            }
        })

        joined_KOW_VMAS_String.leftJoin(klantTable,new ValueJoiner<String, String, VMAS_KOW_Provincie>() {
            @Override
            VMAS_KOW_Provincie apply(String value1, String value2) {
                println("*******")
                println(value1)
                println(value2)
                println("*******")
                VMAS_KansOpWerk vmaskow=gson.fromJson(value1,VMAS_KansOpWerk)

                return new VMAS_KOW_Provincie(
                        ikl:vmaskow.rawEvent.payload.ikl,
                        provincie : gson.fromJson(value2,Klant.class).provincie,
                        kleur: vmaskow.kansOpWerk.color
                )
            }
        }).foreach(new ForeachAction<String, VMAS_KOW_Provincie>() {
            @Override
            void apply(String key, VMAS_KOW_Provincie value) {
                println("JOIN RESULT : ${key} === ${value} ")
            }
        })

//
//        joined_KOW_VMAS.join(klantProvincie, new ValueJoiner<VMAS_KansOpWerk, String, VMAS_KOW_Provincie>() {
//            @Override
//            VMAS_KOW_Provincie apply(VMAS_KansOpWerk value1, String value2) {
//               println("*****  JOIN  met provincie ****")
//                new VMAS_KOW_Provincie(
//                        ikl: '1',//value1.rawEvent.payload.ikl,
//                        kleur:'grr' ,// value1.kansOpWerk.color,
//                        provincie: 'test'  //gson.fromJson(value2,KlantProvincie.class).provincie.naam
//
//                )
//            }
//        })
    }
}
