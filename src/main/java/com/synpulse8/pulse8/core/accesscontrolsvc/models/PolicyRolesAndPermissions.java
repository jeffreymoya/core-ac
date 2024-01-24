package com.synpulse8.pulse8.core.accesscontrolsvc.models;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
@Builder
public class PolicyRolesAndPermissions {
    private static final Pattern PATTERN = Pattern.compile("(definition\\s+(\\w+))|(relation\\s+(\\w+)\\s*:\\s*([^\\r\\n]+)(?:,\\s*([^|]+))?\\s*)|(permission\\s+(\\w+)\\s*=\\s*([^\\r\\n]+)(?:\\s*\\+\\s*([^+]+))?\\s*)");
    private List<Role> roles;
    private List<Permission> permissions;
    private String name;

    public static final String CAVEAT_ATTRIBUTES_MATCH = " with attributes_match";

    @Getter
    @Setter
    @Builder
    public static class Role {
        private String name;
        private List<String> subjects;
    }

    @Getter
    @Setter
    @Builder
    public static class Permission {
        private String name;
        private List<String> rolesOr;
        private List<String> rolesAnd;
    }


    private static PolicyRolesAndPermissions from(String definition) {
        List<Role> roles = new ArrayList<>();
        List<Permission> permissions = new ArrayList<>();

        Matcher matcher = PATTERN.matcher(definition);

        String name = "";
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                name = matcher.group(2);
            } else if (matcher.group(3) != null) {
                String roleName = matcher.group(4);
                String subjectsString = matcher.group(5);
                roles.add(Role.builder()
                        .name(roleName)
                        .subjects(Arrays.stream(
                            subjectsString
                                .replace(CAVEAT_ATTRIBUTES_MATCH, "")
                                .split(" \\| "))
                                .map(String::trim).collect(Collectors.toList())
                        )
                        .build());
            } else if (matcher.group(7) != null) {
                String permissionName = matcher.group(8);
                String rolesString = matcher.group(9);
                permissions.add(Permission.builder()
                        .name(permissionName)
                        //TODO: support other operators in permission aside from "+"
                        .rolesOr(Arrays.stream(rolesString.split("\\+")).map(String::trim).collect(Collectors.toList()))
                        .build());
            }
        }

        return builder().roles(roles).permissions(permissions).name(name).build();
    }

    public static List<PolicyRolesAndPermissions> fromList(String definitions) {
        List<PolicyRolesAndPermissions> policyList = new ArrayList<>();
        String[] definitionBlocks = definitions.split("definition");

        for (String block : definitionBlocks) {
            if (!block.trim().isEmpty()) {
                // Parse each block as a Policy
                PolicyRolesAndPermissions policy = from("definition " + block.trim());
                policyList.add(policy);
            }
        }

        return policyList;
    }

}