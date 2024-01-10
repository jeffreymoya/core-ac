package com.synpulse8.pulse8.core.accesscontrolsvc.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EditRoleDto {
    private String policyName;
    private String updatedRoleName;
    private String currentRoleName;
    private List<String> subjects;
}