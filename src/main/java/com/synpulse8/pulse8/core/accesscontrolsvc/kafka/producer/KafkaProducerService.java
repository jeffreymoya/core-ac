package com.synpulse8.pulse8.core.accesscontrolsvc.kafka.producer;

import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.DeleteRelationshipMessage;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.P8CKafkaTopic;
import org.apache.kafka.common.network.Send;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.synpulse8.pulse8.core.RelationshipCreation;

@Service
public class KafkaProducerService {


    KafkaTemplate<String, String> kafkaTemplate;

    private final KafkaTemplate<String, RelationshipCreation> createRelationshipTemplate;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate,
                                KafkaTemplate<String, RelationshipCreation> createRelationshipTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.createRelationshipTemplate = createRelationshipTemplate;
    }

    public void sendMessage(String message) {

        kafkaTemplate.send(P8CKafkaTopic.CREATE_RELATIONSHIP, "some-key", message);

    }

    public void createRelationship(String message){
        //Creating relationship trigger...
        createRelationshipTemplate.send(P8CKafkaTopic.CREATE_RELATIONSHIP, RelationshipCreation.newBuilder()
                .setObjectId("boson")
                .setObjectType("team")
                .setRelation("member")
                .setOPERATION("OPERATION_TOUCH")
                .setSubjRefObjId("jovie")
                .setSubjRefObjType("user")
                .build()
        );
    }

    public void sendDeleteResourceMessage(String message) {
        kafkaTemplate.send(P8CKafkaTopic.DELETE_RESOURCE, message);
    }



}
