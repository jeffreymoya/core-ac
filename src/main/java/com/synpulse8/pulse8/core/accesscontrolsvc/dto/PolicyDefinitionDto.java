package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.synpulse8.pulse8.core.accesscontrolsvc.models.PolicyMetaData;
import com.synpulse8.pulse8.core.accesscontrolsvc.models.PolicyRolesAndPermissions;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class PolicyDefinitionDto {
    @NotBlank(message = "Policy name is mandatory")
    private String name;
    @NotBlank(message = "Policy description is mandatory")
    private String description;
    @NotEmpty(message = "Policy roles is mandatory")
    private List<PolicyRolesAndPermissions.Role> roles;
    @NotEmpty(message = "Policy permissions is mandatory")
    private List<PolicyRolesAndPermissions.Permission> permissions;
    private Map<String, String> attributes;
    public String toDefinition() {
        StringBuilder definition = new StringBuilder();
        definition.append("definition ")
                .append(this.getName())
                .append(" {\n");

        for (PolicyRolesAndPermissions.Role role : this.getRoles()) {
            definition.append("\trelation ")
                    .append(role.getName())
                    .append(" : ")
                    .append(String.join(" | ", role.getSubjects()))
                    .append("\n");
        }

        for (PolicyRolesAndPermissions.Permission permission : this.getPermissions()) {
            definition.append("\tpermission ")
                    .append(permission.getName())
                    .append(" = ")
                    //TODO: support other operators in permission aside from "+"
                    .append(String.join(" + ", permission.getRolesOr()))
                    .append("\n");
        }

        definition.append("}");
        return definition.toString();
    }

    public PolicyMetaData toMetaData() {
        return PolicyMetaData.builder()
                .name(this.getName())
                .description(this.getDescription())
                .attributes(this.getAttributes())
                .build();
    }

}
