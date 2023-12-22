package com.synpulse8.pulse8.core.accesscontrolsvc.controller;

import com.synpulse8.pulse8.core.accesscontrolsvc.dto.AttributeDefinitionDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.P8CError;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.P8CException;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PolicyDefinitionService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@OpenAPIDefinition(
        info = @Info(title = "Policy API", version = "v1"),
        security = @SecurityRequirement(name = "X-Authenticated-User"))
@ApiResponse(responseCode = "500", description = "An internal error has occurred", content = @Content(schema = @Schema(implementation = P8CError.class)))
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = P8CError.class)))
@RequestMapping(value = "/v1", produces = "application/json")
public class AttributeController {

    private final PolicyDefinitionService policyDefinitionService;

    public AttributeController(PolicyDefinitionService policyDefinitionService) {
        this.policyDefinitionService = policyDefinitionService;
    }

    @RequestMapping(value = "/attributes", method = RequestMethod.POST)
    public void addAttributeDefinition(@RequestBody AttributeDefinitionDto dto) throws P8CException {
        policyDefinitionService.updateAttributeDefinition(dto.getPolicyName(), dto.getAttributes());
    }

    @RequestMapping(value = "/attributes/{policyName}", method = RequestMethod.GET)
    public CompletableFuture<Map<String,Object>> getAttributeDefinitions(@PathVariable String policyName) throws P8CException {
        return policyDefinitionService.viewAttributeDefinitions(policyName);
    }
}
