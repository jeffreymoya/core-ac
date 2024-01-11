package com.synpulse8.pulse8.core.accesscontrolsvc.controller;

import com.synpulse8.pulse8.core.accesscontrolsvc.dto.PolicyDefinitionDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.ReadRelationshipRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.ReadRelationshipResponseDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.ApiError;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.P8CError;
import com.synpulse8.pulse8.core.accesscontrolsvc.models.PolicyMetaData;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PermissionsService;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PolicyDefinitionService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
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
    private final PermissionsService permissionsService;

    public RoleController(PolicyDefinitionService policyDefinitionService, PermissionsService permissionsService) {
        this.policyDefinitionService = policyDefinitionService;
        this.permissionsService = permissionsService;
    }

    /**Note: definition for roles will be added in list of policies as well**/
    @RequestMapping(value = "/roles", method = RequestMethod.POST)
    public CompletableFuture<String> savePolicyDefinition(@RequestBody @Valid PolicyDefinitionDto dto) {
        return policyDefinitionService.save(dto).thenApply(PolicyMetaData::getId);
    }

    @GetMapping("/roles")
    @Operation(description = "Read Relationships", summary = "Endpoint to view roles/relations of a user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully view roles", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReadRelationshipResponseDto.class)))),
            @ApiResponse(responseCode = "403", description = "Forbidden. No permission to view roles", content =  @Content(schema = @Schema(implementation = ApiError.class))),
    })
    @Parameters({
            @Parameter(name = "objectType", in = ParameterIn.QUERY, description = "The type of resource that is requested.", required = true),
            @Parameter(name = "subjRefObjType", in = ParameterIn.QUERY, description = "Type of the subject reference."),
            @Parameter(name = "subjRefObjId", in = ParameterIn.QUERY, description = "ID of the subject reference. Requires subjRefObjType."),
            @Parameter(name = "subjRelation", in = ParameterIn.QUERY, description = "Subject relation. Requires subjRefObjType.")
    })
    public CompletableFuture<Object> viewRolesAndPermissions(@Valid @ModelAttribute ReadRelationshipRequestDto requestParams){

        return policyDefinitionService.getRolesAndPermissionOfUser(permissionsService.readRelationships(requestParams.toReadRelationshipsRequest())
                    .thenApply(ReadRelationshipResponseDto::fromList), requestParams);
    }

}

