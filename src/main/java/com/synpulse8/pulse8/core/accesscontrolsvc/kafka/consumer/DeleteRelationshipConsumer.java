package com.synpulse8.pulse8.core.accesscontrolsvc.kafka.consumer;

import com.synpulse8.pulse8.core.RelationshipCreation;
import com.synpulse8.pulse8.core.RelationshipDeletion;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.DeleteRelationshipRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.DeleteRelationshipMessage;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.P8CKafkaTopic;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PermissionsServiceImpl;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

@Log4j2
@Service
public class DeleteRelationshipConsumer {

    private final PermissionsServiceImpl permissionsService;

    @Autowired
    public DeleteRelationshipConsumer(PermissionsServiceImpl permissionsService){
        this.permissionsService = permissionsService;
    }

    @Getter
    private RelationshipDeletion deleteRelationshipMessage;

    @Getter
    private final CountDownLatch latch = new CountDownLatch(1);
    @KafkaListener(topics = P8CKafkaTopic.DELETE_RESOURCE, groupId = "ac.rel")
    public void receiveDeleteResourceMessage(ConsumerRecord<String, RelationshipDeletion> message) {
        deleteRelationshipMessage = message.value();
        if (StringUtils.isBlank(deleteRelationshipMessage.getObjectId())) {
            log.error(String.format("Invalid resourceId: %s", deleteRelationshipMessage.getObjectId()));
            return;
        }
        if (StringUtils.isBlank(deleteRelationshipMessage.getObjectType())) {
            log.error(String.format("Invalid resourceType: %s", deleteRelationshipMessage.getObjectType()));
            return;
        }

        DeleteRelationshipRequestDto deleteRelationshipRequestDto = DeleteRelationshipRequestDto.builder()
                .objectType(deleteRelationshipMessage.getObjectType())
                .objectId(deleteRelationshipMessage.getObjectId())
                .build();

        permissionsService.deleteRelationships(deleteRelationshipRequestDto.toDeleteRelationshipsRequest());

        log.debug(String.format("Deleted relationship for objectId: %s, objectType: %s", deleteRelationshipMessage.getObjectId(), deleteRelationshipMessage.getObjectType()));

        latch.countDown();
    }

}
