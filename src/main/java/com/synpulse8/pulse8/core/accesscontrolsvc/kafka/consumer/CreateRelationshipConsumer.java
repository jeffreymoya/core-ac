package com.synpulse8.pulse8.core.accesscontrolsvc.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpulse8.pulse8.core.RelationshipCreation;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.WriteRelationshipRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.P8CException;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PermissionsServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class CreateRelationshipConsumer {
    private final PermissionsServiceImpl permissionsService;

    public CreateRelationshipConsumer(PermissionsServiceImpl permissionsService) {
        this.permissionsService = permissionsService;
    }

    @KafkaListener(topics = "${spring.kafka.consumer.default-topic}" , groupId = "${spring.kafka.consumer.group-id}")
    public void createRelationship(ConsumerRecord<String, RelationshipCreation> record) {
        String updates = "{\"updates\":[" + record.value() + "]}";

        try {

            WriteRelationshipRequestDto writeRelationshipRequestDto = new ObjectMapper().readValue(updates, WriteRelationshipRequestDto.class);
            log.info("Consumed details used for creating relationship :\n" + writeRelationshipRequestDto.toWriteRelationshipRequest());
            permissionsService.writeRelationships(writeRelationshipRequestDto.toWriteRelationshipRequest());

        } catch (JsonProcessingException e) {
            log.debug(String.format("Error creating new relationship caused by: " + e.getMessage()));
            throw new P8CException("Error creating new relationship.");
        }
    }

}
