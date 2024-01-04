package com.synpulse8.pulse8.core.accesscontrolsvc.controller;

import com.authzed.api.v1.PermissionService.CheckPermissionResponse;
import com.authzed.api.v1.PermissionService.ExpandPermissionTreeRequest;
import com.authzed.api.v1.PermissionService.ExpandPermissionTreeResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.*;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.ApiError;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.P8CError;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.P8CException;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

    @Value("${p8c.route-check.constants.subjectType}")
    private String subjectType;

    private final ConcurrentMap<String, UriTemplate> uriTemplateCache = new ConcurrentHashMap<>();

    @Autowired
    public PermissionsController(PermissionsService permissionsService) {
        this.permissionsService = permissionsService;
    }

    @PostMapping("/permissions/check")
    @Operation(description = "Check Permissions", summary = "Endpoint to check permissions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully checked permissions", content = @Content(schema = @Schema(implementation = CheckPermissionResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden. No permission to check permissions", content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
    public CompletableFuture<ResponseEntity<Object>> checkPermissions(@RequestBody CheckPermissionRequestDto requestBody) {
        return getResponseEntityCompletableFuture(requestBody);
    }
    @PostMapping("/permissions/route/check")
    @Operation(description = "Check Permissions from route resource", summary = "Endpoint to check permissions from route resource.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully checked permissions from route resource", content = @Content(schema = @Schema(implementation = CheckPermissionResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden. No permission to check permissions from route resource", content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
    public CompletableFuture<ResponseEntity<Object>> routeCheck(@Valid @RequestBody CheckRoutePermissionDto requestBody, HttpServletRequest request) throws JsonProcessingException {
        URI uri = UriComponentsBuilder.fromUriString(requestBody.getRoute()).build().toUri();
        String resourceTypeMatch = "";
        String resourceIdMatch;
        //TODO: Support query parameters from the URL
        if(!StringUtils.isBlank(requestBody.getUriTemplate())) {
            UriTemplate uriTemplate = uriTemplateCache.computeIfAbsent(requestBody.getUriTemplate(), UriTemplate::new);
            Map<String, String> matches = uriTemplate.match(uri.getPath());
            resourceTypeMatch = matches.get("resourceType");
            resourceIdMatch = matches.get("resourceId");
        } else {
            resourceIdMatch = "";
        }

        String objectType = Optional.ofNullable(requestBody.getObjectType()).orElse(resourceTypeMatch);
        String objectId = Optional.ofNullable(requestBody.getObjectId()).orElseGet(
                () -> StringUtils.removeStart(resourceIdMatch, "/"));

        if (StringUtils.isBlank(objectType))
            throw new P8CException("Object type is required when URI Template does not contain resourceType");
        if (StringUtils.isBlank(objectId))
            objectId = "-";

        CheckPermissionRequestDto dto = CheckPermissionRequestDto.builder()
                .permission(requestBody.getMethod().getPermission())
                .subjRefObjId(request.getHeader(subject))
                .subjRefObjType(subjectType)
                .objectType(objectType)
                .objectId(objectId)
                .build();

        LOGGER.debug("CheckPermissionRequestDto: {}", dto);

        return getResponseEntityCompletableFuture(dto);
    }

    private CompletableFuture<ResponseEntity<Object>> getResponseEntityCompletableFuture(CheckPermissionRequestDto dto) {
        return permissionsService.checkPermissions(dto.toCheckPermissionRequest())
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
