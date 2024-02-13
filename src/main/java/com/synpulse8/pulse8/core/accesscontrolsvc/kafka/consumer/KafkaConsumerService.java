package com.synpulse8.pulse8.core.accesscontrolsvc.kafka.consumer;

import com.authzed.api.v1.PermissionService;
import com.synpulse8.pulse8.core.RelationshipCreation;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.WriteSchemaRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.P8CKafkaGroup;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.P8CKafkaTopic;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PermissionsServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class KafkaConsumerService {

    private final PermissionsServiceImpl permissionsService;

    @Autowired
    public KafkaConsumerService(PermissionsServiceImpl permissionsService){
        this.permissionsService = permissionsService;
    }

    @KafkaListener(topics = "create-relationship", groupId = "schema-registry")
    public void receiveMessage(ConsumerRecord<String, RelationshipCreation> record) {
        log.debug(String.format("Consumed message -> %s", record.value()));
    }

//    @KafkaListener(topics = P8CKafkaTopic.CREATE_RELATIONSHIP, groupId = "${consumer.group-id}")
//    public CompletableFuture<PermissionService.WriteRelationshipsResponse> receiveMessage(PermissionService.WriteRelationshipsRequest writeRelationshipsRequest) {
//        //Creating event consumer...
//        return permissionsService.writeRelationships(writeRelationshipsRequest);
//    }

}
