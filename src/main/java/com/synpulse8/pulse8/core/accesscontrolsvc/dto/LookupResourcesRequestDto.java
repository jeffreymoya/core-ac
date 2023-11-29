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
public class LookupResourcesRequestDto extends PermissionRequestDto {
    private String resourceObjectType;

    public PermissionService.LookupResourcesRequest toLookupResourcesRequest() {
        Core.SubjectReference.Builder subjectBuilder = Core.SubjectReference.newBuilder()
                .setObject(
                        Core.ObjectReference.newBuilder()
                                .setObjectType(subjRefObjType)
                                .setObjectId(subjRefObjId)
                                .build());
        if (subjRelation != null && !subjRelation.isEmpty()){
            subjectBuilder.setOptionalRelation(subjRelation);
        }
        Core.SubjectReference subject = subjectBuilder.build();

        return PermissionService.LookupResourcesRequest.newBuilder()
                .setConsistency(
                        PermissionService.Consistency.newBuilder()
                                .setMinimizeLatency(true)
                                .build())
                .setResourceObjectType(resourceObjectType)
                .setSubject(subject)
                .setPermission(permission)
                .build();

    }
}
