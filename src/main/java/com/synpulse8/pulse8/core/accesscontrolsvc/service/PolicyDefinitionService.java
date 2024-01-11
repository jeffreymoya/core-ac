package com.synpulse8.pulse8.core.accesscontrolsvc.service;

import com.authzed.api.v1.SchemaServiceOuterClass;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.PolicyDefinitionDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.ReadRelationshipRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.ReadRelationshipResponseDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.P8CException;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.P8CRelationshipException;
import com.synpulse8.pulse8.core.accesscontrolsvc.models.PolicyMetaData;
import com.synpulse8.pulse8.core.accesscontrolsvc.models.PolicyRolesAndPermissions;
import com.synpulse8.pulse8.core.accesscontrolsvc.repository.PolicyDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PolicyDefinitionService {

    private final PolicyDefinitionRepository policyDefinitionRepository;

    private final SchemaService schemaService;

    private final PermissionsService permissionsService;

    @Autowired
    public PolicyDefinitionService(PolicyDefinitionRepository policyDefinitionRepository, SchemaService schemaService, PermissionsService permissionsService) {
        this.policyDefinitionRepository = policyDefinitionRepository;
        this.schemaService = schemaService;
        this.permissionsService = permissionsService;
    }

    public CompletableFuture<PolicyMetaData> save(PolicyDefinitionDto policyDefinitionDto) {
        String definition = policyDefinitionDto.toDefinition();

        if (policyDefinitionRepository.findByName(policyDefinitionDto.getName()).isPresent()) {
            return CompletableFuture.failedFuture(new P8CException("Policy with the same name already exists: " + policyDefinitionDto.getName()));
        }

        CompletableFuture<String> schemaFuture = fetchSchemaText();

        return schemaFuture.thenCompose(schemaText -> {
            SchemaServiceOuterClass.WriteSchemaRequest requestBody = SchemaServiceOuterClass.WriteSchemaRequest
                    .newBuilder()
                    .setSchema(schemaText + "\n" + definition)
                    .build();
            return schemaService.writeSchema(requestBody)
                    .thenApply(v -> policyDefinitionRepository.save(policyDefinitionDto.toMetaData()));
        });
    }

    public CompletableFuture<List<PolicyDefinitionDto>> getAll() {
        CompletableFuture<String> schemaFuture = fetchSchemaText();

        CompletableFuture<List<PolicyRolesAndPermissions>> rolesAndPermissionsFuture = schemaFuture.thenApply(PolicyRolesAndPermissions::fromList);

        CompletableFuture<List<PolicyMetaData>> metadataFuture = CompletableFuture.supplyAsync(policyDefinitionRepository::findAll);

        return rolesAndPermissionsFuture.thenCombine(metadataFuture, (rolesAndPermissionsList, metadataList) -> {
            Map<String, PolicyMetaData> metaDataMap = metadataList.stream()
                    .collect(Collectors.toMap(PolicyMetaData::getName, Function.identity()));

            return rolesAndPermissionsList.parallelStream()
                    .map(rolesAndPermissions -> {
                        PolicyDefinitionDto.PolicyDefinitionDtoBuilder builder = PolicyDefinitionDto.builder();
                        PolicyMetaData metadata = metaDataMap.get(rolesAndPermissions.getName());
                        builder.name(rolesAndPermissions.getName())
                                .roles(rolesAndPermissions.getRoles())
                                .permissions(rolesAndPermissions.getPermissions());
                        if(metadata != null) {
                            builder
                                    .name(metadata.getName())
                                    .description(metadata.getDescription())
                                    .attributes(metadata.getAttributes())
                                    .access(metadata.getAccess());
                        }
                        return builder.build();
                    })
                    .collect(Collectors.toList());
        });
    }

    private CompletableFuture<String> fetchSchemaText() {
        SchemaServiceOuterClass.ReadSchemaRequest requestBody = SchemaServiceOuterClass.ReadSchemaRequest
                .newBuilder()
                .build();

        return schemaService.readSchema(requestBody)
                .thenApply(SchemaServiceOuterClass.ReadSchemaResponse::getSchemaText);
    }

    public Optional<PolicyMetaData> get(String policyName) {
        return Optional.ofNullable(policyDefinitionRepository.findByName(policyName))
                .orElseThrow(() -> new RuntimeException("Policy not found in MongoDB: " + policyName));
    }

    public void updateAttributeDefinition(String policyName, Map<String, Object> attributes) throws P8CException {
        Optional<PolicyMetaData> policyMetaData = get(policyName);

        if (policyMetaData.isEmpty()) {
            throw new P8CException("Policy not found in MongoDB: " + policyName);
        }

        policyMetaData.ifPresent( policy -> {
            attributes.entrySet().stream()
            .forEach(entry -> {
                policy.getAttributes().put(entry.getKey(), entry.getValue());
                });
            policyDefinitionRepository.save(policy);
        });

    }

    public CompletableFuture<Map<String,Object>> viewAttributeDefinitions(String policyName) throws P8CException {
        Optional<PolicyMetaData> policyMetaData = get(policyName);
        CompletableFuture<Map<String, Object>> attributesMap = new CompletableFuture<>();

        if (policyMetaData.isEmpty()) {
            throw new P8CException("Policy not found in MongoDB: " + policyName);
        }

        CompletableFuture.runAsync(() -> {
             policyMetaData.ifPresent( policy -> {
                 Map<String, Object> attributes = policy.getAttributes();
                 attributesMap.complete(attributes);
             });
        });

        return attributesMap;
    }

    public CompletableFuture<PolicyDefinitionDto> getPolicyDefinition(String resourceName) throws P8CException{

        Optional<PolicyMetaData> policyMetaData = policyDefinitionRepository.findByName(resourceName);

        CompletableFuture<String> schemaFuture = fetchSchemaText();
        CompletableFuture<List<PolicyRolesAndPermissions>> rolesAndPermissionsFuture = schemaFuture.thenApply(PolicyRolesAndPermissions::fromList);
        PolicyDefinitionDto.PolicyDefinitionDtoBuilder builder = PolicyDefinitionDto.builder();

        return rolesAndPermissionsFuture.thenApply(rolesAndPermission -> {
            builder.name(resourceName);
            Optional<PolicyRolesAndPermissions> policyRolesAndPermissions =  rolesAndPermission.stream()
                    .filter(item -> item.getName().equals(resourceName))
                    .findAny();

            if (policyMetaData.isEmpty() && policyRolesAndPermissions.isEmpty()) {
                throw new P8CException("Policy not found in MongoDB/spiceDB: " + resourceName);
            }

            policyMetaData.ifPresent( policy -> {
                builder.description(policy.getDescription())
                        .attributes(policy.getAttributes())
                        .access(policy.getAccess());
            });
            policyRolesAndPermissions.ifPresent( item -> {
                builder.roles(item.getRoles())
                        .permissions(item.getPermissions());
            });
            return builder.build();
        });
    }

    public CompletableFuture<Void> deletePolicyRole(String resourceName, String roleName) {
        // Check relationships of the role under the resource
        ReadRelationshipRequestDto readRelationshipRequestDto = ReadRelationshipRequestDto.builder()
                .objectType(resourceName)
                .relation(roleName)
                .build();
        return permissionsService.readRelationships(readRelationshipRequestDto.toReadRelationshipsRequest())
                .thenCompose(result -> {
                    List<ReadRelationshipResponseDto> relationshipList = ReadRelationshipResponseDto.fromList(result);
                    if (!relationshipList.isEmpty()) {
                        return CompletableFuture.failedFuture(new P8CRelationshipException("Cannot delete role `" + roleName + "` in policy `" + resourceName + "`, as a relationship exists under it", Collections.singletonList(relationshipList)));
                    }

                    return getPolicyDefinition(resourceName).thenCompose(policy -> {
                        // Delete role on relation
                        boolean hasDeletedRole = policy.getRoles().removeIf(role -> roleName.equals(role.getName()));

                        if (!hasDeletedRole) {
                            return CompletableFuture.failedFuture(new P8CException("Role `" + roleName + "` not found under policy `" + resourceName + "`"));
                        }

                        // Delete roleName on permissions
                        policy.getPermissions().forEach(permission -> {
                            Optional.ofNullable(permission.getRolesOr()).ifPresent(rolesOr -> {
                                rolesOr.remove(roleName);
                            });
                        });

                        // Update policy with new roles
                        String policyText = policy.toDefinition();
                        CompletableFuture<String> schemaFuture = fetchSchemaText();
                        return schemaFuture.thenCompose(schemaText -> {
                            String updatedSchemaText = updateDefinition(schemaText, resourceName, policyText);
                            SchemaServiceOuterClass.WriteSchemaRequest requestBody = SchemaServiceOuterClass.WriteSchemaRequest
                                    .newBuilder()
                                    .setSchema(updatedSchemaText)
                                    .build();
                            return schemaService.writeSchema(requestBody)
                                    .thenCompose(x -> CompletableFuture.completedFuture(null));
                        });
                    });
        });
    }

    public static String updateDefinition(String schemaText, String keyword, String replacement) {
        String pattern = "definition\\s+" + keyword + "\\s*\\{[^{}]*\\}";
        Pattern regex = Pattern.compile(pattern, Pattern.DOTALL);
        Matcher matcher = regex.matcher(schemaText);
        if (matcher.find()) return schemaText.replace(matcher.group(), replacement);
        return schemaText;
    }

}