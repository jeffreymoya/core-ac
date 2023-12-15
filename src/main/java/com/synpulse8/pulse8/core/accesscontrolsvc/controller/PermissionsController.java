package com.synpulse8.pulse8.core.accesscontrolsvc.controller;

import com.authzed.api.v1.PermissionService.CheckPermissionResponse;
import com.authzed.api.v1.PermissionService.DeleteRelationshipsRequest;
import com.authzed.api.v1.PermissionService.DeleteRelationshipsResponse;
import com.authzed.api.v1.PermissionService.ExpandPermissionTreeRequest;
import com.authzed.api.v1.PermissionService.ExpandPermissionTreeResponse;
import com.authzed.api.v1.PermissionService.ReadRelationshipsRequest;
import com.authzed.api.v1.PermissionService.ReadRelationshipsResponse;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.CheckPermissionRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.CheckRoutePermissionDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.LookupResourcesRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.LookupResourcesResponseDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.LookupSubjectsRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.LookupSubjectsResponseDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.WriteRelationshipRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.ApiError;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.P8CError;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PermissionsService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@OpenAPIDefinition(
        info = @Info(title = "Permissions API", version = "v1"),
        security = @SecurityRequirement(name = "X-Authenticated-User"))
@ApiResponse(responseCode = "500", description = "An internal error has occurred", content = @Content(schema = @Schema(implementation = P8CError.class)))
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = P8CError.class)))
@RequestMapping(value = "/v1", produces = "application/json")
public class PermissionsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsController.class);

    private final PermissionsService permissionsService;

    @Value("${p8c.security.principal-header}")
    private String subject;

    @Value("${p8c.route-check.constants.objectType}")
    private String objectType;

    @Value("${p8c.route-check.constants.subjRefObjType}")
    private String subjRefObjType;

    @Autowired
    public PermissionsController(PermissionsService permissionsService) {
        this.permissionsService = permissionsService;
    }

    @PostMapping("/relationships/write")
    @Operation(description = "Write Relationships", summary = "Endpoint to write relationships.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully wrote relationships", content = @Content(schema = @Schema(implementation = WriteRelationshipRequestDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden. No permission to write relationships", content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
    public CompletableFuture<ResponseEntity<String>> writeRelationships(@RequestBody WriteRelationshipRequestDto requestBody) {
        return permissionsService.writeRelationships(requestBody.toWriteRelationshipRequest())
                .thenApply(x -> ResponseEntity.ok(x.getWrittenAt().getToken()));
    }

    @PostMapping("/relationships/read")
    @Operation(description = "Read Relationships", summary = "Endpoint to read relationships.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully read relationships", content = @Content(schema = @Schema(implementation = ReadRelationshipsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden. No permission to read relationships", content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
    public CompletableFuture<ResponseEntity<Iterator<ReadRelationshipsResponse>>> readRelationships(@RequestBody ReadRelationshipsRequest requestBody) {
        return permissionsService.readRelationships(requestBody)
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/relationships/delete")
    @Operation(description = "Delete Relationships", summary = "Endpoint to delete relationships.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted relationships", content = @Content(schema = @Schema(implementation = DeleteRelationshipsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden. No permission to delete relationships", content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
    public CompletableFuture<ResponseEntity<DeleteRelationshipsResponse>> deleteRelationships(@RequestBody DeleteRelationshipsRequest requestBody) {
        return permissionsService.deleteRelationships(requestBody)
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/permissions/check")
    @Operation(description = "Check Permissions", summary = "Endpoint to check permissions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully checked permissions", content = @Content(schema = @Schema(implementation = CheckPermissionResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden. No permission to check permissions", content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
    public CompletableFuture<ResponseEntity<Object>> checkPermissions(@RequestBody CheckPermissionRequestDto requestBody) {
        return getCheckPermissionAndTransformResponse(requestBody);
    }
    @PostMapping("/permissions/route/check")
    @Operation(description = "Check Permissions", summary = "Endpoint to check permissions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully checked permissions", content = @Content(schema = @Schema(implementation = CheckPermissionResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden. No permission to check permissions", content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
    public CompletableFuture<ResponseEntity<Object>> routeCheck(@Valid @RequestBody CheckRoutePermissionDto requestBody, HttpServletRequest request) {
        URI uri = UriComponentsBuilder.fromUriString(requestBody.getRoute()).build().toUri();
        CheckPermissionRequestDto checkRoutePermissionDto = CheckPermissionRequestDto.builder()
                .permission(requestBody.getMethod().getPermission())
                .subjRefObjId(request.getHeader(subject))
                .subjRefObjType(subjRefObjType)
                .objectType(objectType)
                .objectId(uri.getPath())
                .build();

        return getCheckPermissionAndTransformResponse(checkRoutePermissionDto);
    }

    private CompletableFuture<ResponseEntity<Object>> getCheckPermissionAndTransformResponse(CheckPermissionRequestDto checkRoutePermissionDto) {
        return permissionsService.checkPermissions(checkRoutePermissionDto.toCheckPermissionRequest())
                .thenApply(x -> {
                    if (x.getPermissionship() == CheckPermissionResponse.Permissionship.PERMISSIONSHIP_HAS_PERMISSION) {
                        return ResponseEntity.ok(Collections.singletonMap("has_permission", true));
                    } else {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("has_permission", false));
                    }
                });
    }

    @PostMapping("/permissions/expand")
    @Operation(description = "Expand Permission Tree", summary = "Endpoint to expand permission tree.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully expanded permission tree", content = @Content(schema = @Schema(implementation = ExpandPermissionTreeResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden. No permission to expand permission tree", content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
    public CompletableFuture<ResponseEntity<ExpandPermissionTreeResponse>> expandPermissions(@RequestBody ExpandPermissionTreeRequest requestBody) {
        return permissionsService.expandPermissions(requestBody)
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/permissions/resources")
    @Operation(description = "Lookup Resources", summary = "Endpoint to lookup resources.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully looked up resources", content = @Content(schema = @Schema(implementation = LookupResourcesResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden. No permission to lookup resources", content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
    public CompletableFuture<ResponseEntity<List<LookupResourcesResponseDto>>> lookupResources(@Valid @RequestBody LookupResourcesRequestDto requestBody) {
        return permissionsService.lookupResources(requestBody.toLookupResourcesRequest())
                .thenApply(list -> ResponseEntity.ok(LookupResourcesResponseDto.fromList(list)));
    }

    @PostMapping("/permissions/subjects")
    @Operation(description = "Lookup Subjects", summary = "Endpoint to lookup subjects.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully looked up subjects", content = @Content(schema = @Schema(implementation = LookupSubjectsResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden. No permission to lookup subjects", content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
    public CompletableFuture<ResponseEntity<List<LookupSubjectsResponseDto>>> lookupSubjects(@RequestBody LookupSubjectsRequestDto requestBody) {
        return permissionsService.lookupSubjects(requestBody.toLookupSubjectsRequest())
                .thenApply(list -> ResponseEntity.ok(LookupSubjectsResponseDto.fromList(list)));
    }
}
