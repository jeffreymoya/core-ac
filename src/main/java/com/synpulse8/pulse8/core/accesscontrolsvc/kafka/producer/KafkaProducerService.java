package com.synpulse8.pulse8.core.accesscontrolsvc.kafka.producer;

import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.P8CKafkaTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message) {
        kafkaTemplate.send(P8CKafkaTopic.ARTICLE, "somekey", message);
    }

}
