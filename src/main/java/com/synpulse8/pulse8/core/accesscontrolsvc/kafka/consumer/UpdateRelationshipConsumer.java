package com.synpulse8.pulse8.core.accesscontrolsvc.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
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

        if(record.value().getRelation() != null && !record.value().getRelation().isEmpty()){
            ArrayList<String> relationList = new ArrayList<>(Arrays.asList(record.value().getRelation().split(",")));
            StringBuilder recordListBuilder = new StringBuilder();

            relationList.stream().forEach(x -> {
                RelationshipUpdate relationshipUpdate = new RelationshipUpdate();
                relationshipUpdate.setObjectType(record.value().getObjectType());
                relationshipUpdate.setObjectId(record.value().getObjectId());
                relationshipUpdate.setRelation(x);
                relationshipUpdate.setSubjRefObjId(record.value().getSubjRefObjId());
                relationshipUpdate.setSubjRefObjType(record.value().getSubjRefObjType());
                relationshipUpdate.setSubjRelation(record.value().getSubjRelation());
                relationshipUpdate.setOperation("OPERATION_TOUCH");
                recordListBuilder.append(relationshipUpdate).append(",");
            });

            String updates = "{\"updates\":[" + recordListBuilder.substring(0, recordListBuilder.length() - 1) + "]}";

            try {

                WriteRelationshipRequestDto writeRelationshipRequestDto = new ObjectMapper().readValue(updates, WriteRelationshipRequestDto.class);
                log.info("Consumed details used for updating relationship :\n" + writeRelationshipRequestDto.toWriteRelationshipRequest());
                permissionsService.writeRelationships(writeRelationshipRequestDto.toWriteRelationshipRequest());

            } catch (JsonProcessingException e) {
                log.debug(String.format("Error updating relationship caused by: " + e.getMessage()));
                throw new P8CException("Error updating relationship.");
            }

            latch.countDown();
        }
    }

}
