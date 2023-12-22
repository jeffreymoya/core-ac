package com.synpulse8.pulse8.core.accesscontrolsvc.controller;

import com.authzed.api.v1.PermissionService.CheckPermissionResponse;
import com.authzed.api.v1.PermissionService.ExpandPermissionTreeRequest;
import com.authzed.api.v1.PermissionService.ExpandPermissionTreeResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.*;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    @Value("${p8c.route-check.constants.subjRefObjType}")
    private String subjRefObjType;

    //TODO: make this configurable
    private UriTemplate uriTemplate = new UriTemplate("/{resourceType}{/?}{resourceId:.*}");

    @Value("${p8c.security.roles-header}")
    private String roles;

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

    @GetMapping("/relationships")
    @Operation(description = "Read Relationships", summary = "Endpoint to read relationships.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully read relationships", content = @Content(schema = @Schema(implementation = ReadRelationshipResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden. No permission to read relationships", content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
    public CompletableFuture<ResponseEntity<List<ReadRelationshipResponseDto>>> readRelationships(@ModelAttribute ReadRelationshipRequestDto requestBody) {
        return permissionsService.readRelationships(requestBody.toReadRelationshipsRequest())
                .thenApply(x -> ResponseEntity.ok(ReadRelationshipResponseDto.fromList(x)));
    }

    @DeleteMapping("/relationships")
    @Operation(description = "Delete Relationships", summary = "Endpoint to delete relationships by filter.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted relationships", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden. No permission to delete relationships", content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
    public CompletableFuture<ResponseEntity<String>> deleteRelationshipsByFilter(@ModelAttribute DeleteRelationshipRequestDto requestBody) {
        return permissionsService.deleteRelationships(requestBody.toDeleteRelationshipsRequest())
                .thenApply(x -> ResponseEntity.noContent().build());
    }

    @DeleteMapping(value = {
            "/relationships/{objectType}/{objectId}/{relation}/{subjRefObjType}/{subjRefObjId}",
            "/relationships/{objectType}/{objectId}/{relation}/{subjRefObjType}/{subjRefObjId}/{subjRelation}"
    })
    @Operation(description = "Delete Relationship", summary = "Endpoint to delete relationship by path.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted relationships", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden. No permission to delete relationships", content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
    public CompletableFuture<ResponseEntity<Void>> deleteRelationshipsByPath(@ModelAttribute DeleteRelationshipRequestDto requestBody) {
        return permissionsService.deleteRelationships(requestBody.toDeleteRelationshipsRequest())
                .thenApply(x -> ResponseEntity.noContent().build());
    }

    @PostMapping("/permissions/check")
    @Operation(description = "Check Permissions", summary = "Endpoint to check permissions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully checked permissions", content = @Content(schema = @Schema(implementation = CheckPermissionResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden. No permission to check permissions", content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
    public CompletableFuture<ResponseEntity<Object>> checkPermissions(@RequestBody CheckPermissionRequestDto requestBody) {
        return permissionsService.checkPermissions(requestBody.toCheckPermissionRequest())
                .thenApply(x -> {
                    if (x.getPermissionship() == CheckPermissionResponse.Permissionship.PERMISSIONSHIP_HAS_PERMISSION) {
                        return ResponseEntity.ok(Collections.singletonMap("has_permission", true));
                    } else {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("has_permission", false));
                    }
                });
    }
    @PostMapping("/permissions/route/check")
    @Operation(description = "Check Permissions from route resource", summary = "Endpoint to check permissions from route resource.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully checked permissions from route resource", content = @Content(schema = @Schema(implementation = CheckPermissionResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden. No permission to check permissions from route resource", content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
    public CompletableFuture<ResponseEntity<Object>> routeCheck(@Valid @RequestBody CheckRoutePermissionDto requestBody, HttpServletRequest request) throws JsonProcessingException {
        URI uri = UriComponentsBuilder.fromUriString(requestBody.getRoute()).build().toUri();
        byte[] decode = Base64.getDecoder().decode(request.getHeader(roles));
        String roles = new String(decode);
        LOGGER.debug("Roles: {}", roles);
        //TODO: Accept a URL Template from the request to tell which part of the URL is the resourceType and the resourceId
        //TODO: Support query parameters from the URL
        Map<String, String> matches = uriTemplate.match(uri.getPath());
        String objectType = matches.get("resourceType");
        String objectid = "-"; //default index if there's no resourceId

        if(StringUtils.isNotEmpty(matches.get("resourceId"))) {
            objectid = StringUtils.removeStart("/", matches.get("resourceId"));
        }

        CheckPermissionRequestDto dto = CheckPermissionRequestDto.builder()
                .permission(requestBody.getMethod().getPermission())
                .subjRefObjId(request.getHeader(subject))
                .objectType(objectType)
                .objectId(objectid)
                .build();

        return permissionsService.bulkCheckPermissions(dto, roles).thenApply(hasPermission -> {
            if (hasPermission) {
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
