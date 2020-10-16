package vdab.demo

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import groovy.transform.CompileStatic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.protocol.types.Field
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.kstream.Aggregator
import org.apache.kafka.streams.kstream.Initializer
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.ValueMapper
import org.springframework.stereotype.Component
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Printed
import scala.Tuple3

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

    void startUp(){
        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime,new LocalDateTimeAdapter()).create()



        StreamsBuilder builder   = new StreamsBuilder();

        KStream<String,String> stream= builder.stream( "Axon.VMAS.DomainEvents")

        KStream<String,RawEvent> resultStream= stream.mapValues(new ValueMapper<String, RawEvent>() {
            @Override
            RawEvent apply(String value) {
                gson.fromJson(value,RawEvent.class)
            }
        })

         KStream<String, Integer> aggregateStream= resultStream.groupByKey().aggregate(
                new Initializer<Integer>() {
                    @Override
                    Integer apply() {
                        return 0
                    }
                    },
        new Aggregator<String, RawEvent, Integer>() {
            @Override
            Integer apply(String key, RawEvent value, Integer  aggregate) {
                return aggregate + value.payload.aantalVacatures()
            }
        }
, Materialized.with(Serdes.String(),Serdes.Integer())
        )
       .toStream()
        aggregateStream.to("OUT_Aggregate")


        Topology topo = builder.build();
        KafkaStreams streams =new KafkaStreams(topo,CreateProperties());

        streams.start()




    }
}
