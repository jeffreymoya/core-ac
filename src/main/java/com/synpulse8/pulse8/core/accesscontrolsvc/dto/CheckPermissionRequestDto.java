package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.authzed.api.v1.Core;
import com.authzed.api.v1.PermissionService;
import com.google.protobuf.Struct;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class CheckPermissionRequestDto extends PermissionRequestDto {
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

        PermissionService.CheckPermissionRequest.Builder builder = PermissionService.CheckPermissionRequest.newBuilder()
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
                .setPermission(permission);

        if (context != null && !context.isEmpty()) {
            Struct contextBuilder = Struct.newBuilder()
                    .putAllFields(ContextMapper.convertMap(context))
                    .build();
            builder.setContext(contextBuilder);
        }

        return builder.build();

    }
}
