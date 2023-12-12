package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.authzed.api.v1.Core;
import com.authzed.api.v1.PermissionService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class CheckRoutePermissionDto extends PermissionRequestDto {
    private String route;
    private String httpMethod;
    public PermissionService.CheckPermissionRequest toCheckPermissionRequest() {
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

        return PermissionService.CheckPermissionRequest.newBuilder()
                .setConsistency(
                        PermissionService.Consistency.newBuilder()
                                .setMinimizeLatency(true)
                                .build())
                .setResource(
                        Core.ObjectReference.newBuilder()
                                .setObjectType(objectType)
                                .setObjectId(objectId)
                                .build())
                .setSubject(subject)
                .setPermission(permission)
                .build();

    }
}
