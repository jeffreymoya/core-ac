package com.synpulse8.pulse8.core.accesscontrolsvc.controller;

import com.synpulse8.pulse8.core.accesscontrolsvc.dto.PolicyDefinitionDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.UpdatePolicyDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.P8CError;
import com.synpulse8.pulse8.core.accesscontrolsvc.models.PolicyMetaData;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PolicyDefinitionService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@OpenAPIDefinition(
        info = @Info(title = "Policy API", version = "v1"),
        security = @SecurityRequirement(name = "X-Authenticated-User"))
@ApiResponse(responseCode = "500", description = "An internal error has occurred", content = @Content(schema = @Schema(implementation = P8CError.class)))
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = P8CError.class)))
@RequestMapping(value = "/v1", produces = "application/json")
public class PolicyController {

    private final PolicyDefinitionService policyDefinitionService;

    public PolicyController(PolicyDefinitionService policyDefinitionService) {
        this.policyDefinitionService = policyDefinitionService;
    }

    @RequestMapping(value = "/policy", method = RequestMethod.POST)
    public CompletableFuture<String> savePolicyDefinition(@RequestBody @Valid PolicyDefinitionDto dto) {
        return policyDefinitionService.save(dto).thenApply(PolicyMetaData::getId);
    }

    @RequestMapping(value = "/policy", method = RequestMethod.PUT)
    public CompletableFuture<PolicyDefinitionDto> updatePolicyDefinition(@RequestBody @Valid UpdatePolicyDto dto) {
        return policyDefinitionService.updatePolicyDefinition(dto);
    }

    @RequestMapping(value = "/policies", method = RequestMethod.GET)
    public CompletableFuture<List<PolicyDefinitionDto>> getAllPolicyDefinitions() {
        return policyDefinitionService.getAll();
    }

    @RequestMapping(value = "/policies/{resourceName}", method = RequestMethod.GET)
    public CompletableFuture<PolicyDefinitionDto> getPolicyDefinition(@PathVariable String resourceName) {
        return policyDefinitionService.getPolicyDefinition(resourceName);
    }
}
