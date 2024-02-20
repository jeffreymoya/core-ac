package com.synpulse8.pulse8.core.accesscontrolsvc.kafka.consumer;

import com.synpulse8.pulse8.core.accesscontrolsvc.dto.DeleteRelationshipRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.DeleteRelationshipMessage;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.P8CKafkaGroup;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.P8CKafkaTopic;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PermissionsServiceImpl;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import com.synpulse8.pulse8.core.RelationshipCreation;

import java.util.concurrent.CountDownLatch;

@Log4j2
@Service
public class KafkaConsumerService {

    private final PermissionsServiceImpl permissionsService;

    @Autowired
    public KafkaConsumerService(PermissionsServiceImpl permissionsService){
        this.permissionsService = permissionsService;
    }

    @KafkaListener(topics = "create-relationship", groupId = "ac.rel")
    public void receiveMessage(ConsumerRecord<String, RelationshipCreation> record) {
        log.info(String.format("Consumed message -> %s", record.value()));
    }

    //    @KafkaListener(topics = P8CKafkaTopic.CREATE_RELATIONSHIP, groupId = "${consumer.group-id}")
    //    public CompletableFuture<PermissionService.WriteRelationshipsResponse> receiveMessage(PermissionService.WriteRelationshipsRequest writeRelationshipsRequest) {
    //        //Creating event consumer...
    //        return permissionsService.writeRelationships(writeRelationshipsRequest);
    //    }

}
