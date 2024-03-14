package com.synpulse8.pulse8.core.accesscontrolsvc.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpulse8.pulse8.core.RelationshipCreation;
import com.synpulse8.pulse8.core.RelationshipUpdate;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.DeleteRelationshipRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.WriteRelationshipRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.P8CException;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PermissionsServiceImpl;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;

@Log4j2
@Service
public class UpdateRelationshipConsumer {

    private final PermissionsServiceImpl permissionsService;

    @Getter
    private final CountDownLatch latch = new CountDownLatch(1);


    public UpdateRelationshipConsumer(PermissionsServiceImpl permissionsService) {
        this.permissionsService = permissionsService;
    }

    @KafkaListener(topics = "${p8c.kafka.topics.update_relationship}" , groupId = "${spring.kafka.consumer.group-id}")
    public void updateRelationship(ConsumerRecord<String, RelationshipUpdate> record) {

        String updates = "{\"updates\":[" + record.value() + "]}";

        try {

            WriteRelationshipRequestDto writeRelationshipRequestDto = new ObjectMapper().readValue(updates, WriteRelationshipRequestDto.class);
            log.info("Consumed details used for updating relationship :\n" + writeRelationshipRequestDto.toWriteRelationshipRequest());
            permissionsService.writeRelationships(writeRelationshipRequestDto.toWriteRelationshipRequest());

        } catch (JsonProcessingException e) {
            log.debug(String.format("Error updating relationship caused by: " + e.getMessage()));
            throw new P8CException("Error updating relationship.");
        }
    }

}
