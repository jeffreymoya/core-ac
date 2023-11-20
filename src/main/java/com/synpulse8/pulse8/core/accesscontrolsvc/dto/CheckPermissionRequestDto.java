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
    private String permission;

    public PermissionService.CheckPermissionRequest toCheckPermissionRequest() {

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
                .setSubject(
                        Core.SubjectReference.newBuilder()
                                .setObject(
                                        Core.ObjectReference.newBuilder()
                                                .setObjectType(subjRefObjType)
                                                .setObjectId(subjRefObjId)
                                                .build())
                                .build())
                .setPermission(permission)
                .build();

    }
}
