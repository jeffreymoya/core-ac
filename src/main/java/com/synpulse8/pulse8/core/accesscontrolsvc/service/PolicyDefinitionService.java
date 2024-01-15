package com.synpulse8.pulse8.core.accesscontrolsvc.service;

import com.authzed.api.v1.PermissionService;
import com.authzed.api.v1.SchemaServiceOuterClass;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.EditRoleDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.PolicyDefinitionDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.ReadRelationshipResponseDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.RelationshipRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.RolesAndPermissionDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.ReadRelationshipRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.ReadRelationshipResponseDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.P8CException;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.P8CRelationshipException;
import com.synpulse8.pulse8.core.accesscontrolsvc.models.PolicyMetaData;
import com.synpulse8.pulse8.core.accesscontrolsvc.models.PolicyRolesAndPermissions;
import com.synpulse8.pulse8.core.accesscontrolsvc.repository.PolicyDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PolicyDefinitionService {

    private final PolicyDefinitionRepository policyDefinitionRepository;

    private final SchemaService schemaService;
    private PermissionsService permissionsService;

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
    public CompletableFuture<PolicyDefinitionDto> update(PolicyDefinitionDto policyDefinitionDto) {
        CompletableFuture<PolicyDefinitionDto> policyDefinitionFuture = getPolicyDefinition(policyDefinitionDto.getName());

        CompletableFuture<String> schemaFuture = fetchSchemaText();

        return schemaFuture.thenCombine(policyDefinitionFuture, (schemaText, object) -> {
            if (object == null) {
                throw new P8CException("Policy not found: " + policyDefinitionDto.getName());
            }

            PolicyDefinitionDto dto = (PolicyDefinitionDto) object;
            String updatedSchemaText = schemaText.replace(dto.toDefinition(), policyDefinitionDto.toDefinition());

            SchemaServiceOuterClass.WriteSchemaRequest requestBody = SchemaServiceOuterClass.WriteSchemaRequest
                    .newBuilder()
                    .setSchema(updatedSchemaText)
                    .build();

            schemaService.writeSchema(requestBody).join();
            return dto;
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

    public CompletableFuture<EditRoleDto> editRole(EditRoleDto editRoleDto) {
        ReadRelationshipRequestDto dto = ReadRelationshipRequestDto.builder()
                .objectType(editRoleDto.getPolicyName())
                .relation(editRoleDto.getCurrentRoleName())
                .build();

        CompletableFuture<Iterator<PermissionService.ReadRelationshipsResponse>> relationshipsFuture = permissionsService.readRelationships(dto.toReadRelationshipsRequest());
        CompletableFuture<PolicyDefinitionDto> policyDefinitionFuture = getPolicyDefinition(editRoleDto.getPolicyName());

        return relationshipsFuture.thenCombine(policyDefinitionFuture, (relationships, object) -> {
            if (relationships.hasNext()) {
                throw new P8CException("Relationship exists");
            }
            PolicyDefinitionDto policyDefinition = (PolicyDefinitionDto) object;

            boolean roleExistsInPermissions = policyDefinition.getPermissions().parallelStream()
                    .flatMap(p -> Stream.concat(
                            Optional.ofNullable(p.getRolesAnd()).orElse(List.of()).stream(),
                            Optional.ofNullable(p.getRolesOr()).orElse(List.of()).stream()))
                    .anyMatch(role -> role.equals(editRoleDto.getCurrentRoleName()));

            if (roleExistsInPermissions) {
                throw new P8CException("Role is assigned to a permission");
            }

            PolicyRolesAndPermissions.Role originalRole = policyDefinition.getRoles().stream()
                    .filter(role -> role.getName().equals(editRoleDto.getCurrentRoleName()))
                    .findFirst()
                    .orElseThrow(() -> new P8CException("Role not found"));
            originalRole.setName(editRoleDto.getUpdatedRoleName());
            originalRole.setSubjects(editRoleDto.getSubjects());

            update(policyDefinition).join();

            return editRoleDto;
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
                        return CompletableFuture.failedFuture(new P8CRelationshipException("Cannot delete role `" + roleName + "` in policy `" + resourceName + "`, as a relationship exists under it", relationshipList));
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
                        return update(policy).thenCompose(x -> CompletableFuture.completedFuture(null));
                    });
        });
    }


    public CompletableFuture<Object> getRolesAndPermissionOfUser(CompletableFuture<List<ReadRelationshipResponseDto>> relationships,
                                                                 RelationshipRequestDto requestParams){

        Optional<PolicyMetaData> policyMetaData = policyDefinitionRepository.findByName(requestParams.getObjectType());

        CompletableFuture<String> schemaFuture = fetchSchemaText();
        CompletableFuture<List<PolicyRolesAndPermissions>> rolesAndPermissionsFuture = schemaFuture.thenApply(PolicyRolesAndPermissions::fromList);

        return rolesAndPermissionsFuture.thenCombine(relationships, (rolesAndPermissionsList, relationList) ->{
            List<String> roles = relationList.stream().map( relationship -> relationship.getRelation()).collect(Collectors.toList());

            Optional<PolicyRolesAndPermissions> policyRolesAndPermissions =  rolesAndPermissionsList.stream()
                    .filter(item -> item.getName().equals(requestParams.getObjectType()))
                    .findAny();

            RolesAndPermissionDto.RolesAndPermissionDtoBuilder builder = RolesAndPermissionDto.builder();
            builder.objectType(requestParams.getObjectType());
            builder.subjRefObjType(requestParams.getSubjRefObjType());
            builder.subjRefObjId(requestParams.getSubjRefObjId());
            builder.subjRelation(requestParams.getSubjRelation());
            builder.roles(roles);

            List<PolicyRolesAndPermissions.Permission> filterPermissions = new ArrayList<>();
            policyRolesAndPermissions.ifPresent( item -> {
                item.getPermissions().stream().forEach(
                        permissions -> {
                            permissionChecker(permissions,roles,filterPermissions);
                        }
                );
                builder.permissions(filterPermissions);
            });
            return builder.build();
        });
    }

    public List<PolicyRolesAndPermissions.Permission> permissionChecker(PolicyRolesAndPermissions.Permission permissions, List<String> roles, List<PolicyRolesAndPermissions.Permission> filteredPermissions){

        if(permissions.getRolesOr() != null){
            for(String role : roles){
                permissions.getRolesOr().stream().forEach(
                        permission -> {
                            if(permission.contains(role) && !filteredPermissions.contains(permissions)){
                                filteredPermissions.add(permissions);
                            }
                        }
                );
            }
        }

        if(permissions.getRolesAnd() != null){
            permissions.getRolesAnd().stream().forEach(
                    permission -> {
                        if(roles.contains(permission) && !filteredPermissions.contains(permissions)){
                            filteredPermissions.add(permissions);
                        }
                    }
            );
        }

        return filteredPermissions;
    }
}