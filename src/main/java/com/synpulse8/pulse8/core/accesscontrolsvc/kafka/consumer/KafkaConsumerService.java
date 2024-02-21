package com.synpulse8.pulse8.core.accesscontrolsvc.kafka.consumer;

import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.P8CKafkaGroup;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.P8CKafkaTopic;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class KafkaConsumerService {

    @KafkaListener(topics = P8CKafkaTopic.ARTICLE, groupId = P8CKafkaGroup.MAIN)
    public void receiveMessage(String message) {
        // Process the received message
        log.debug("Received message: " + message);
    }

}
