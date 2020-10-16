package vdab.demo

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.Topology
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
@SpringBootApplication
class Application {

    @Autowired EventProcessor eventProcessor
    static void main(String... args){
        SpringApplication.run(Application.class, args)
    }

    @PostConstruct
    StartStreamProcessors(){


        eventProcessor.startUp()
    }


}
