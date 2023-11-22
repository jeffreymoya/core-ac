package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.authzed.api.v1.Core;
import com.authzed.api.v1.PermissionService;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckPermissionRequestDto {
    private String objectType;
    private String objectId;
    private String subjRefObjType;
    private String subjRefObjId;
    private String subjRelation;
    private String permission;

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
