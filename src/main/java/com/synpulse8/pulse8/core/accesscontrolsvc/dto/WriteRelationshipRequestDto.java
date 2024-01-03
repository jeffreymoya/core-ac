package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.authzed.api.v1.Core;
import com.authzed.api.v1.PermissionService;
import com.google.protobuf.Struct;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class WriteRelationshipRequestDto {

    @Valid
    private List<WriteRelationshipUpdate> updates;

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    private static class WriteRelationshipUpdate extends RelationshipRequestDto {

        @Schema(description = "Relationship update operation", example = "OPERATION_CREATE")
        protected Core.RelationshipUpdate.Operation operation = Core.RelationshipUpdate.Operation.OPERATION_CREATE;

        @NotBlank(message = "Object ID cannot be null")
        @Schema(description = "The ID of the resource that is requested", example = "doc001")
        protected String objectId;

        @NotBlank(message = "Relation cannot be null")
        @Schema(description = "The relation of the subject to the resource", example = "doc001")
        protected String relation;

        @NotBlank(message = "Subject reference object type cannot be null")
        @Schema(description = "Type of the subject reference", example ="user")
        protected String subjRefObjType;

        @NotBlank(message = "Subject reference object ID cannot be null")
        @Schema(description = "ID of the subject reference", example = "john01")
        protected String subjRefObjId;

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

    public Core.RelationshipUpdate buildRelationshipUpdate(WriteRelationshipUpdate update) {
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
        Core.Relationship.Builder relationshipBuilder = Core.Relationship.newBuilder()
                .setResource(resource)
                .setRelation(update.relation)
                .setSubject(subject);

        if (update.caveatName != null && !update.caveatName.isEmpty()) {
            Core.ContextualizedCaveat.Builder caveatBuilder = Core.ContextualizedCaveat.newBuilder()
                    .setCaveatName(update.caveatName);
            if (update.context != null && !update.context.isEmpty()) {
                Struct contextBuilder = Struct.newBuilder()
                        .putAllFields(ContextMapper.convertMap(update.context))
                        .build();
                caveatBuilder.setContext(contextBuilder);
            }
            relationshipBuilder.setOptionalCaveat(caveatBuilder.build());
        }

        Core.Relationship relationship = relationshipBuilder.build();

        Core.RelationshipUpdate relationshipUpdate = Core.RelationshipUpdate.newBuilder()
                .setOperation(update.operation)
                .setRelationship(relationship)
                .build();
        return relationshipUpdate;
    }
}
