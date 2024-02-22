package com.synpulse8.pulse8.core.accesscontrolsvc.kafka.producer;

import com.synpulse8.pulse8.core.RelationshipCreation;
import com.synpulse8.pulse8.core.RelationshipDeletion;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.P8CKafkaTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DeleteRelationshipProducer {


    private final KafkaTemplate<String, RelationshipDeletion> kafkaTemplate;

    @Autowired
    public DeleteRelationshipProducer(KafkaTemplate<String, RelationshipDeletion> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendDeleteResourceMessage(RelationshipDeletion message) {
        kafkaTemplate.send(P8CKafkaTopic.DELETE_RESOURCE, message);
    }
}
