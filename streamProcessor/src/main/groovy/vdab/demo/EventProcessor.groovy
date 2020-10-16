package vdab.demo

import groovy.transform.CompileStatic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.kstream.KStream
import org.springframework.stereotype.Component
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Printed

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

        StreamsBuilder builder   = new StreamsBuilder();

        KStream<String,String> stream= builder.stream( "Axon.VMAS.DomainEvents")
        Printed<String,String> sysOut=Printed.toSysOut()
        stream.print(sysOut)

        Topology topo = builder.build();
        KafkaStreams streams =new KafkaStreams(topo,CreateProperties());

        streams.start()




    }
}
