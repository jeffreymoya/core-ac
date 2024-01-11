package com.synpulse8.pulse8.core.accesscontrolsvc.controller;

import com.synpulse8.pulse8.core.accesscontrolsvc.dto.PolicyDefinitionDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.P8CError;
import com.synpulse8.pulse8.core.accesscontrolsvc.models.PolicyMetaData;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PolicyDefinitionService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@OpenAPIDefinition(
        info = @Info(title = "Policy API", version = "v1"),
        security = @SecurityRequirement(name = "X-Authenticated-User"))
@ApiResponse(responseCode = "500", description = "An internal error has occurred", content = @Content(schema = @Schema(implementation = P8CError.class)))
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = P8CError.class)))
@RequestMapping(value = "/v1", produces = "application/json")
public class RoleController {
    private final PolicyDefinitionService policyDefinitionService;

    public RoleController(PolicyDefinitionService policyDefinitionService) {
        this.policyDefinitionService = policyDefinitionService;
    }

    /**Note: definition for roles will be added in list of policies as well**/
    @RequestMapping(value = "/roles", method = RequestMethod.POST)
    public CompletableFuture<String> savePolicyDefinition(@RequestBody @Valid PolicyDefinitionDto dto) {
        return policyDefinitionService.save(dto).thenApply(PolicyMetaData::getId);
    }

    @RequestMapping(value = "/roles/{resourceName}/{roleName}", method = RequestMethod.DELETE)
    @Operation(description = "Delete Role", summary = "Endpoint to delete role.")
    @Parameters({
            @Parameter(name = "resourceName", in = ParameterIn.PATH, description = "The name of the resource."),
            @Parameter(name = "roleName", in = ParameterIn.PATH, description = "The name of the role.")
    })
    public CompletableFuture<ResponseEntity<Void>> deletePolicyRole(@PathVariable String resourceName, @PathVariable String roleName) {
        return policyDefinitionService.deletePolicyRole(resourceName, roleName)
                .thenApply(x -> ResponseEntity.noContent().build());
    }
}

