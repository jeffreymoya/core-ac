package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.synpulse8.pulse8.core.accesscontrolsvc.enums.Access;
import com.synpulse8.pulse8.core.accesscontrolsvc.models.PolicyMetaData;
import com.synpulse8.pulse8.core.accesscontrolsvc.models.PolicyRolesAndPermissions;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

import static com.synpulse8.pulse8.core.accesscontrolsvc.models.PolicyRolesAndPermissions.CAVEAT_ATTRIBUTES_MATCH;

@Getter
@Builder
public class PolicyDefinitionDto {
    @NotBlank(message = "Policy name is mandatory")
    private String name;
    @NotBlank(message = "Policy description is mandatory")
    private String description;
    @NotEmpty(message = "Policy roles is mandatory")
    private List<PolicyRolesAndPermissions.Role> roles;
    private List<PolicyRolesAndPermissions.Permission> permissions;
    private Map<String, Object> attributes;
    @NotNull(message = "Policy access is mandatory")
    private Access access;
    public String toDefinition() {
        StringBuilder definition = new StringBuilder();
        String attributesMatch;
        if (this.getAttributes() != null && !this.getAttributes().isEmpty()) {
            attributesMatch = CAVEAT_ATTRIBUTES_MATCH;
        } else {
            attributesMatch = "";
        }
        definition.append("definition ")
                .append(this.getName())
                .append(" {\n");

        for (PolicyRolesAndPermissions.Role role : this.getRoles()) {
            definition.append("\trelation ")
                    .append(role.getName())
                    .append(" : ")
                    .append(String.join(" | ", role.getSubjects().stream().map(s -> s + attributesMatch).toList()))
                    .append("\n");
        }

        if(this.getPermissions() != null){
            for (PolicyRolesAndPermissions.Permission permission : this.getPermissions()) {
                definition.append("\tpermission ")
                        .append(permission.getName())
                        .append(" = ")
                        //TODO: support other operators in permission aside from "+"
                        .append(String.join(" + ", permission.getRolesOr()))
                        .append("\n");
            }
        }

        definition.append("}");
        return definition.toString();
    }

    public PolicyMetaData toMetaData() {
        return PolicyMetaData.builder()
                .name(this.getName())
                .description(this.getDescription())
                .attributes(this.getAttributes())
                .access(this.getAccess())
                .build();
    }

}
