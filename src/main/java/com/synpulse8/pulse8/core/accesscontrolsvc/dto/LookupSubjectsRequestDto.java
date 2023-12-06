package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.authzed.api.v1.Core;
import com.authzed.api.v1.PermissionService;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class LookupSubjectsRequestDto extends PermissionRequestDto {
    @Schema(description = "Type of the subject", example = "user")
    @NotNull(message = "Type of subject cannot be null")
    private String subjectObjectType;

    public PermissionService.LookupSubjectsRequest toLookupSubjectsRequest() {
        PermissionService.LookupSubjectsRequest.Builder subjectBuilder = PermissionService.LookupSubjectsRequest.newBuilder()
                .setConsistency(
                        PermissionService.Consistency.newBuilder()
                                .setMinimizeLatency(true)
                                .build())
                .setResource(
                        Core.ObjectReference.newBuilder()
                                .setObjectType(objectType)
                                .setObjectId(objectId)
                                .build());

        if (subjRelation != null && !subjRelation.isEmpty()){
            subjectBuilder.setOptionalSubjectRelation(subjRelation);
        }

        return subjectBuilder.setPermission(permission)
                    .setSubjectObjectType(subjectObjectType)
                    .build();

    }

    @Override
    @Schema(hidden = true)
    public String getSubjRefObjType() {
        return subjectObjectType;
    }

    @Override
    @Schema(hidden = true)
    public String getSubjRefObjId() {
        return null;
    }
}
