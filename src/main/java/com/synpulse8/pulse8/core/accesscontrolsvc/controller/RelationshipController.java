package com.synpulse8.pulse8.core.accesscontrolsvc.controller;

import com.synpulse8.pulse8.core.accesscontrolsvc.dto.*;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.ApiError;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.P8CError;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.producer.CreateRelationshipProducer;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PermissionsService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@OpenAPIDefinition(
        info = @Info(title = "Relationships API", version = "v1"),
        security = @SecurityRequirement(name = "X-Authenticated-User"))
@ApiResponse(responseCode = "500", description = "An internal error has occurred", content = @Content(schema = @Schema(implementation = P8CError.class)))
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = P8CError.class)))
@RequestMapping(value = "/v1", produces = "application/json")
public class RelationshipController {

    private final PermissionsService permissionsService;

    private final CreateRelationshipProducer createRelationshipProducer;

    public RelationshipController(PermissionsService permissionsService, CreateRelationshipProducer createRelationshipProducer) {
        this.permissionsService = permissionsService;
        this.createRelationshipProducer = createRelationshipProducer;
    }

    @PostMapping("/relationships")
    @Operation(description = "Write Relationships", summary = "Endpoint to write relationships.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully wrote relationships", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden. No permission to write relationships", content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
    public CompletableFuture<ResponseEntity<String>> writeRelationships(@Valid @RequestBody WriteRelationshipRequestDto requestBody) {
        return permissionsService.writeRelationships(requestBody.toWriteRelationshipRequest())
                .thenApply(x -> ResponseEntity.ok(x.getWrittenAt().getToken()));
    }

    @GetMapping("/relationships")
    @Operation(description = "Read Relationships", summary = "Endpoint to read relationships.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully read relationships", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReadRelationshipResponseDto.class)))),
            @ApiResponse(responseCode = "403", description = "Forbidden. No permission to read relationships", content =  @Content(schema = @Schema(implementation = ApiError.class))),
    })
    @Parameters({
            @Parameter(name = "objectType", in = ParameterIn.QUERY, description = "The type of resource that is requested.", required = true),
            @Parameter(name = "objectId", in = ParameterIn.QUERY, description = "The ID of the resource that is requested."),
            @Parameter(name = "relation", in = ParameterIn.QUERY, description = "The relation of the subject to the resource."),
            @Parameter(name = "subjRefObjType", in = ParameterIn.QUERY, description = "Type of the subject reference."),
            @Parameter(name = "subjRefObjId", in = ParameterIn.QUERY, description = "ID of the subject reference. Requires subjRefObjType."),
            @Parameter(name = "subjRelation", in = ParameterIn.QUERY, description = "Subject relation. Requires subjRefObjType."),
    })
    public CompletableFuture<ResponseEntity<List<ReadRelationshipResponseDto>>> readRelationships(@Valid @ModelAttribute ReadRelationshipRequestDto requestParams) {
        return permissionsService.readRelationships(requestParams.toReadRelationshipsRequest())
                .thenApply(x -> ResponseEntity.ok(ReadRelationshipResponseDto.fromList(x)));
    }

    @DeleteMapping(value = {
            "/relationships/{objectType}/{objectId}/{relation}/{subjRefObjType}/{subjRefObjId}",
            "/relationships/{objectType}/{objectId}/{relation}/{subjRefObjType}/{subjRefObjId}/{subjRelation}"
    })
    @Operation(description = "Delete Relationship", summary = "Endpoint to delete relationship by path.")
    @Parameters({
            @Parameter(name = "objectType", in = ParameterIn.PATH, description = "The type of resource that is requested."),
            @Parameter(name = "objectId", in = ParameterIn.PATH, description = "The ID of the resource that is requested."),
            @Parameter(name = "relation", in = ParameterIn.PATH, description = "The relation of the subject to the resource."),
            @Parameter(name = "subjRefObjType", in = ParameterIn.PATH, description = "Type of the subject reference."),
            @Parameter(name = "subjRefObjId", in = ParameterIn.PATH, description = "ID of the subject reference."),
            @Parameter(name = "subjRelation", in = ParameterIn.PATH, description = "Subject relation."),
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted relationships"),
            @ApiResponse(responseCode = "403", description = "Forbidden. No permission to delete relationships", content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
    public CompletableFuture<ResponseEntity<Void>> deleteRelationshipsByPath(@ModelAttribute DeleteRelationshipRequestDto requestBody) {
        return permissionsService.deleteRelationships(requestBody.toDeleteRelationshipsRequest())
                .thenApply(x -> ResponseEntity.noContent().build());
    }


    @DeleteMapping("/relationships")
    @Operation(description = "Delete Relationships", summary = "Endpoint to delete relationships by filter.")
    @Parameters({
            @Parameter(name = "objectType", in = ParameterIn.QUERY, description = "The type of resource that is requested.", required = true),
            @Parameter(name = "objectId", in = ParameterIn.QUERY, description = "The ID of the resource that is requested."),
            @Parameter(name = "relation", in = ParameterIn.QUERY, description = "The relation of the subject to the resource."),
            @Parameter(name = "subjRefObjType", in = ParameterIn.QUERY, description = "Type of the subject reference."),
            @Parameter(name = "subjRefObjId", in = ParameterIn.QUERY, description = "ID of the subject reference. Requires subjRefObjType."),
            @Parameter(name = "subjRelation", in = ParameterIn.QUERY, description = "Subject relation. Requires subjRefObjType."),
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted relationships"),
            @ApiResponse(responseCode = "403", description = "Forbidden. No permission to delete relationships", content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
    public CompletableFuture<ResponseEntity<Void>> deleteRelationshipsByFilter(@Valid @ModelAttribute DeleteRelationshipRequestDto requestParams) {
        return permissionsService.deleteRelationships(requestParams.toDeleteRelationshipsRequest())
                .thenApply(x -> ResponseEntity.noContent().build());
    }

    @PostMapping("/relationships/create")
    @Operation(description = "Store Relationships", summary = "Endpoint to store relationships.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully wrote relationships", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden. No permission to write relationships", content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
    public void createRelationship(@Valid @RequestBody RelationshipRequestDto requestBody) {
        createRelationshipProducer.createRelationship(requestBody);
    }
}
