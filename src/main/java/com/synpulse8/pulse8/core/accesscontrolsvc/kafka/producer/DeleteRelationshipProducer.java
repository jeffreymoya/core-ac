package com.synpulse8.pulse8.core.accesscontrolsvc.kafka.producer;

import com.synpulse8.pulse8.core.RelationshipCreation;
import com.synpulse8.pulse8.core.RelationshipDeletion;
import com.synpulse8.pulse8.core.SpecificRelationshipDeletion;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.P8CKafkaTopic;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class DeleteRelationshipProducer {


    @Value("${p8c.kafka.topics.update_delete_relationship}")
    String deleteSpecificRelationshipTopic;
    private final KafkaTemplate<String, RelationshipDeletion> kafkaTemplate;

    private final KafkaTemplate<String, SpecificRelationshipDeletion> deleteRelationshipTemplate;

    @Autowired
    public DeleteRelationshipProducer(KafkaTemplate<String, RelationshipDeletion> kafkaTemplate, KafkaTemplate<String, SpecificRelationshipDeletion> deleteRelationshipTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.deleteRelationshipTemplate = deleteRelationshipTemplate;
    }

    public void sendDeleteResourceMessage(RelationshipDeletion message) {
        kafkaTemplate.send(P8CKafkaTopic.DELETE_RESOURCE, message);
    }

    public void sendDeleteSpecificRelationshipMessage(SpecificRelationshipDeletion specificRelationshipDeletion){
        log.info("received message: " + specificRelationshipDeletion.toString());
        deleteRelationshipTemplate.send(deleteSpecificRelationshipTopic, specificRelationshipDeletion);
    }
}
