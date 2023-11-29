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
public class PermissionRequestDto {
    protected String objectType;
    protected String objectId;
    protected String subjRefObjType;
    protected String subjRefObjId;
    protected String subjRelation;
    protected String permission;
}
