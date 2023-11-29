package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.authzed.api.v1.Core;
import com.authzed.api.v1.PermissionService;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class LookupSubjectsRequestDto extends PermissionRequestDto {
    private String subjectObjectType;

    public PermissionService.LookupSubjectsRequest toLookupSubjectsRequest() {
        return PermissionService.LookupSubjectsRequest.newBuilder()
                .setConsistency(
                        PermissionService.Consistency.newBuilder()
                                .setMinimizeLatency(true)
                                .build())
                .setResource(
                        Core.ObjectReference.newBuilder()
                                .setObjectType(objectType)
                                .setObjectId(objectId)
                                .build())
                .setPermission(permission)
                .setSubjectObjectType(subjectObjectType)
                .build();

    }
}
