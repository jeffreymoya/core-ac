package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.synpulse8.pulse8.core.accesscontrolsvc.models.PolicyRolesAndPermissions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
@Builder
public class RolesAndPermissionDto {

    @Schema(description = "The type of resource that is requested", example = "policy")
    protected String objectType;

    @Schema(description = "Type of the subject reference", example ="user")
    protected String subjRefObjType;

    @Schema(description = "ID of the subject reference", example = "john01")
    protected String subjRefObjId;

    @Schema(description = "Subject relation", example = "employee")
    protected String subjRelation;

    private List<String> roles;
    private List<PolicyRolesAndPermissions.Permission> permissions;
}
