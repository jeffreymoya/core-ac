package com.synpulse8.pulse8.core.accesscontrolsvc.kafka.producer;

import com.authzed.api.v1.PermissionService;
import com.synpulse8.pulse8.core.RelationshipCreation;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.RelationshipRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.WriteRelationshipRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.WriteSchemaRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.P8CKafkaTopic;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.common.network.Send;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, RelationshipCreation> createRelationshipTemplate;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, RelationshipCreation> createRelationshipTemplate) {
        this.createRelationshipTemplate = createRelationshipTemplate;
    }

    public void createRelationship(RelationshipRequestDto relationshipRequestDto){
        log.info("Triggered relationship creation for subjRefObjType (" +
                relationshipRequestDto.getSubjRefObjType() + ") with id (" +
                relationshipRequestDto.getSubjRefObjId() + ")");

        createRelationshipTemplate.send(P8CKafkaTopic.CREATE_RELATIONSHIP, RelationshipCreation.newBuilder()
                .setObjectId(relationshipRequestDto.getObjectId())
                .setObjectType(relationshipRequestDto.getObjectType())
                .setRelation(relationshipRequestDto.getRelation())
                .setSubjRefObjId(relationshipRequestDto.getSubjRefObjId())
                .setSubjRefObjType(relationshipRequestDto.getSubjRefObjType())
                .setSubjRelation(relationshipRequestDto.getSubjRelation())
                .build()
        );

    }



}
