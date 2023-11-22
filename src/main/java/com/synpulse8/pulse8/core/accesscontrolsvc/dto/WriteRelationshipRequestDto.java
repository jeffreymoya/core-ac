package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.authzed.api.v1.Core;
import com.authzed.api.v1.PermissionService;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class WriteRelationshipRequestDto {

    private List<Update> updates;

    @Getter
    @NoArgsConstructor
    private static class Update {
        private String objectType;
        private String objectId;
        private String relation;
        private String subjRefObjType;
        private String subjRefObjId;
        private String subjRelation;
        private static final Core.RelationshipUpdate.Operation operation = Core.RelationshipUpdate.Operation.OPERATION_CREATE;

    }

    public PermissionService.WriteRelationshipsRequest toWriteRelationshipRequest() {
        List<Core.RelationshipUpdate> relationshipUpdates = updates
                .stream()
                .map(this::buildRelationshipUpdate)
                .collect(Collectors.toList());
        PermissionService.WriteRelationshipsRequest request = PermissionService.WriteRelationshipsRequest.newBuilder()
                .addAllUpdates(relationshipUpdates)
                .build();
        return request;
    }

    public Core.RelationshipUpdate buildRelationshipUpdate(Update update) {
        Core.ObjectReference resource = Core.ObjectReference.newBuilder()
                .setObjectType(update.objectType)
                .setObjectId(update.objectId)
                .build();
        Core.ObjectReference object = Core.ObjectReference.newBuilder()
                .setObjectType(update.subjRefObjType)
                .setObjectId(update.subjRefObjId)
                .build();
        Core.SubjectReference.Builder subjectBuilder = Core.SubjectReference.newBuilder()
                .setObject(object);
        if(update.subjRelation != null && !update.subjRelation.isEmpty()) {
            subjectBuilder.setOptionalRelation(update.subjRelation);
        }
        Core.SubjectReference subject = subjectBuilder.build();

        Core.RelationshipUpdate relationshipUpdate = Core.RelationshipUpdate.newBuilder()
                .setOperation(update.operation)
                .setRelationship(
                        Core.Relationship.newBuilder()
                                .setResource(resource)
                                .setRelation(update.relation)
                                .setSubject(subject)
                                .build())
                .build();
        return relationshipUpdate;
    }
}
