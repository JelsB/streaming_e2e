package vdab.demo

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import groovy.transform.CompileStatic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.kstream.Aggregator
import org.apache.kafka.streams.kstream.Initializer
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.KeyValueMapper
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
    KTable<String,String> kansOpWerkTable
    KTable<String,KlantProvincie> klantInfo

    void startUp(){
        gson = new GsonBuilder().registerTypeAdapter(LocalDateTime,new LocalDateTimeAdapter()).serializeNulls().create()

        StreamsBuilder builder   = new StreamsBuilder();

        kansOpWerkProcessor(builder)
        KStream<String,VMAS_KansOpWerk> joined= Join_VMAS_KOW(builder)
        joined.mapValues(new ValueMapper<VMAS_KansOpWerk, String>() {
            @Override
            String apply(VMAS_KansOpWerk value) {
                return gson.toJson(value)
            }
        })
        .to("JOINED_KOW_VMAS")
        vmasAggregate_Kleur(joined)

        createKlantProvincieStream(builder)

        Topology topo = builder.build();
        KafkaStreams streams =new KafkaStreams(topo,CreateProperties());

        streams.start()


    }



    def kansOpWerkProcessor(StreamsBuilder builder) {
        KStream<String,String> stream= builder.stream( "kansopwerk")

        KStream<String,KansOpWerk> rawStream=stream.mapValues(new ValueMapper<String, KansOpWerk>() {
            @Override
            KansOpWerk apply(String value) {
                return gson.fromJson(value,KansOpWerk.class)
            }
        })

        kansOpWerkTable=rawStream.mapValues(
                new ValueMapper<KansOpWerk, String>() {
                    @Override
                    String apply(KansOpWerk value) {
                        return gson.toJson(value)
                    }
                }
        ) .toTable()

    }


    KTable<String,String> createKlantProvincieStream (StreamsBuilder builder){
        KStream<String,String> klantStreamRaw=builder.stream("vdp.public.klant")
        KStream<String,String> provincieStreamRaw=builder.stream("vdp.public.provincie")

        KStream<String,String> klantStr= klantStreamRaw.mapValues(new ValueMapper<String, Klant>() {
            @Override
            Klant apply(String value) {
                return (Klant) gson.fromJson(extractAfterCange(value), Klant.class )
            }
        }) .selectKey(new KeyValueMapper<String, Klant, String>() {
            @Override
            String apply(String key, Klant value) {
                return value.provincieid.toString()
            }
        }).mapValues(new ValueMapper<Klant, String>() {
            @Override
            String apply(Klant value) {
                return gson.toJson(value)
            }
        })

        KStream<Integer,Provincie> provincieStr=provincieStreamRaw.mapValues(new ValueMapper<String, Provincie>() {
            @Override
            Provincie apply(String value) {
                return (Provincie) gson.fromJson(extractAfterCange(value), Provincie.class )

            }
        }).selectKey(new KeyValueMapper<String, Provincie, Integer>() {
            @Override
            Integer apply(String key, Provincie value) {
                return value.provincieid
            }
        })

        KTable<String,String> provintieTable = provincieStr.mapValues(new ValueMapper<Provincie, String>() {
            @Override
            String apply(Provincie value) {
                return gson.toJson(value)
            }
        }).selectKey(new KeyValueMapper<Integer, String, String>() {
            @Override
            String apply(Integer key, String value) {
                return key.toString()
            }
        }).toTable()

        KStream<String,KlantProvincie> klantProvStr= klantStr.join(provintieTable,
                new ValueJoiner<String, String, KlantProvincie>() {
            @Override
            KlantProvincie apply(String value1, String value2) {
                println("*****  JOINING *****  klant ${value1} --- ${value2}")
                //return new KlantProvincie(kant:value1,provincie: gson.fromJson(value2,Provincie.class) )
//                KlantProvincie klprov=new KlantProvincie(klant: gson.fromJson(value1,Klant.class) ,provincie: gson.fromJson(value2,Provincie.class) )
//                klprov
                KlantProvincie klprov=new KlantProvincie(klant: gson.fromJson(value1,Klant.class) ,provincie: gson.fromJson(value2,Provincie.class) )
                return klprov
            }
        } )
        KStream<String,String> rekeyAndSerialized= klantProvStr.selectKey(new KeyValueMapper<String, KlantProvincie, String>() {
            @Override
            String apply(String key, KlantProvincie  value) {
                return value.klant.ikl.toString()
            }
        }).
                mapValues(new ValueMapper<KlantProvincie, String>() {
                    @Override
                    String apply(KlantProvincie value) {
                        return gson.toJson(value)
                    }
                })
        return rekeyAndSerialized.toTable()


    }

    KStream<String,VMAS_KansOpWerk> Join_VMAS_KOW(StreamsBuilder builder){

        KStream<String,String> stream= builder.stream( "Axon.VMAS.DomainEvents")

        KStream<String,RawEvent> resultStream= stream.mapValues(new ValueMapper<String, RawEvent>() {
            @Override
            RawEvent apply(String value) {
                gson.fromJson(value,RawEvent.class)
            }
        })

         KStream<String,VMAS_KansOpWerk> joined=resultStream.leftJoin(kansOpWerkTable,new ValueJoiner<RawEvent, String, VMAS_KansOpWerk>() {
            @Override
            VMAS_KansOpWerk apply(RawEvent value1, String value2) {
                new VMAS_KansOpWerk(rawEvent: value1,kansOpWerk: gson.fromJson(value2,KansOpWerk.class)  )
            }
        })

        return joined

    }

    def vmasAggregate_Kleur(KStream<String,VMAS_KansOpWerk> vmasJoin){

        KStream<String, String> aggregateStream= vmasJoin

                .selectKey(new KeyValueMapper<String, VMAS_KansOpWerk, String>() {
            @Override
            String apply(String key, VMAS_KansOpWerk value) {
                return   value.kansOpWerk.color
            }
        })
        .mapValues(new ValueMapper<VMAS_KansOpWerk, String>() {
            @Override
            String apply(VMAS_KansOpWerk value) {
                return gson.toJson(value)
            }
        })
                .groupByKey().aggregate(
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
                        println "AGGREGATE --- ${aggregate}"
                        AggregateRecord aggregateObject=null
                        try {
                            aggregateObject = gson.fromJson(aggregate, AggregateRecord.class)
                        }
                        catch (Exception exception){
                            aggregateObject=new AggregateRecord()
                        }
                        aggregateObject.doAggregation(joined.rawEvent.payload.aantalVacatures())
                        aggregateObject.kleur=joined.kansOpWerk.color
                        return gson.toJson(aggregateObject)
                    }
                }
        )
                .toStream()
        aggregateStream.print(Printed.toSysOut())
        aggregateStream.to("OUT_KleurAantalVerzonden")
    }
}
