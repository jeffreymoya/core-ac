package com.synpulse8.pulse8.core.accesscontrolsvc.service;

import com.authzed.api.v1.SchemaServiceOuterClass;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.PolicyDefinitionDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.ReadRelationshipResponseDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.RelationshipRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.RolesAndPermissionDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.P8CException;
import com.synpulse8.pulse8.core.accesscontrolsvc.models.PolicyMetaData;
import com.synpulse8.pulse8.core.accesscontrolsvc.models.PolicyRolesAndPermissions;
import com.synpulse8.pulse8.core.accesscontrolsvc.repository.PolicyDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PolicyDefinitionService {

    private final PolicyDefinitionRepository policyDefinitionRepository;

    private final SchemaService schemaService;

    @Autowired
    public PolicyDefinitionService(PolicyDefinitionRepository policyDefinitionRepository, SchemaService schemaService) {
        this.policyDefinitionRepository = policyDefinitionRepository;
        this.schemaService = schemaService;
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
                            if(permissions.getRolesOr() != null){
                                permissions.getRolesOr().stream().forEach(
                                        permission -> {
                                            if(roles.contains(permission) && !filterPermissions.contains(permissions)){
                                                filterPermissions.add(permissions);
                                            }
                                        }
                                );
                            }

                            if(permissions.getRolesAnd() != null){
                                permissions.getRolesAnd().stream().forEach(
                                        permission -> {
                                            if(roles.contains(permission) && !filterPermissions.contains(permissions)){
                                                filterPermissions.add(permissions);
                                            }
                                        }
                                );
                            }
                        }
                );
                builder.permissions(filterPermissions);
            });
            return builder.build();
        });
    }
}