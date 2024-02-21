package com.synpulse8.pulse8.core.accesscontrolsvc.kafka.producer;

import com.synpulse8.pulse8.core.RelationshipCreation;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.RelationshipRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.P8CKafkaTopic;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class CreateRelationshipProducer {

    private final KafkaTemplate<String, RelationshipCreation> createRelationshipTemplate;

    public CreateRelationshipProducer(KafkaTemplate<String, RelationshipCreation> createRelationshipTemplate) {
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
