package com.synpulse8.pulse8.core.accesscontrolsvc.kafka.producer;

import com.authzed.api.v1.PermissionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpulse8.pulse8.core.RelationshipCreation;
import com.synpulse8.pulse8.core.RelationshipUpdate;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.WriteRelationshipRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.P8CException;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class UpdateRelationshipProducer {

    @Value("${p8c.kafka.topics.update_relationship}")
    String updateRelationshipTopic;

    private final KafkaTemplate<String, RelationshipUpdate> updateCreateRelationshipTemplate;


    public UpdateRelationshipProducer(KafkaTemplate<String, RelationshipUpdate> updateCreateRelationshipTemplate) {
        this.updateCreateRelationshipTemplate = updateCreateRelationshipTemplate;
    }

    public void sendUpdateRelationshipMessage(RelationshipUpdate writeRelationshipsRequest){
        updateCreateRelationshipTemplate.send(updateRelationshipTopic, writeRelationshipsRequest);
    }

}
