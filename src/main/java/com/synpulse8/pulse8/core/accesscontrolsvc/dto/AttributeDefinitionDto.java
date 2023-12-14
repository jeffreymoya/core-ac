package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class AttributeDefinitionDto {

    @NotBlank(message = "Policy name is mandatory")
    private String policyName;
    @NotBlank(message = "Attributes is mandatory")
    private Map<String, Object> attributes;

}
