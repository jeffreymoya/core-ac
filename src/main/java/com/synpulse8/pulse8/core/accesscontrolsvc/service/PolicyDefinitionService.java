package com.synpulse8.pulse8.core.accesscontrolsvc.service;

import com.authzed.api.v1.SchemaServiceOuterClass;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.PolicyDefinitionDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.models.PolicyMetaData;
import com.synpulse8.pulse8.core.accesscontrolsvc.models.PolicyRolesAndPermissions;
import com.synpulse8.pulse8.core.accesscontrolsvc.repository.PolicyDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        CompletableFuture<String> schemaFuture = fetchSchemaText();

        return schemaFuture.thenApply(schemaText -> {
            SchemaServiceOuterClass.WriteSchemaRequest requestBody = SchemaServiceOuterClass.WriteSchemaRequest
                    .newBuilder()
                    .setSchema(schemaText + "\\n" + definition)
                    .build();
            schemaService.writeSchema(requestBody);
            return policyDefinitionRepository.save(policyDefinitionDto.toMetaData());
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
                        if(metadata != null) {
                            builder
                                    .name(metadata.getName())
                                    .type(metadata.getType())
                                    .description(metadata.getDescription())
                                    .attributes(metadata.getAttributes());
                        }
                        return builder
                                .roles(rolesAndPermissions.getRoles())
                                .permissions(rolesAndPermissions.getPermissions())
                                .build();
                    })
                    .collect(Collectors.toList());
        });
    }

    private CompletableFuture<String> fetchSchemaText() {
        SchemaServiceOuterClass.ReadSchemaRequest requestBody = SchemaServiceOuterClass.ReadSchemaRequest
                .newBuilder()
                .build();

        CompletableFuture<String> schemaFuture = schemaService.readSchema(requestBody)
                .thenApply(SchemaServiceOuterClass.ReadSchemaResponse::getSchemaText);
        return schemaFuture;
    }

    public PolicyMetaData get(String policyName) {
        return Optional.ofNullable(policyDefinitionRepository.findByName(policyName))
                .orElseThrow(() -> new RuntimeException("Policy not found in MongoDB: " + policyName));
    }
}