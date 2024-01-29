package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.synpulse8.pulse8.core.accesscontrolsvc.enums.Access;
import com.synpulse8.pulse8.core.accesscontrolsvc.models.PolicyRolesAndPermissions;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UpdatePolicyDto {
    @NotBlank(message = "Policy name is mandatory")
    private String name;
    private String updatedName;
    private String description;
    private List<PolicyRolesAndPermissions.Permission> permissions;
    private Access access;
}
