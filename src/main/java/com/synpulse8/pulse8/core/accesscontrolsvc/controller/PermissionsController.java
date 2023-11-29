package com.synpulse8.pulse8.core.accesscontrolsvc.controller;

import com.authzed.api.v1.PermissionService.*;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.CheckPermissionRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.LookupResourcesRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.LookupResourcesResponseDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.LookupSubjectsRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.LookupSubjectsResponseDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.WriteRelationshipRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PermissionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(value = "/v1", produces = "application/json")
public class PermissionsController {

    private final PermissionsService permissionsService;

    @Autowired
    public PermissionsController(PermissionsService permissionsService) {
        this.permissionsService = permissionsService;
    }

    @PostMapping("/relationships/write")
    public CompletableFuture<ResponseEntity<String>> writeRelationships(@RequestBody WriteRelationshipRequestDto requestBody) {
        return permissionsService.writeRelationships(requestBody.toWriteRelationshipRequest())
                .thenApply(x -> ResponseEntity.ok(x.getWrittenAt().getToken()));
    }

    @PostMapping("/relationships/read")
    public CompletableFuture<ResponseEntity<Iterator<ReadRelationshipsResponse>>> readRelationships(@RequestBody ReadRelationshipsRequest requestBody) {
        return permissionsService.readRelationships(requestBody)
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/relationships/delete")
    public CompletableFuture<ResponseEntity<DeleteRelationshipsResponse>> deleteRelationships(@RequestBody DeleteRelationshipsRequest requestBody) {
        return permissionsService.deleteRelationships(requestBody)
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/permissions/check")
    public CompletableFuture<ResponseEntity<String>> checkPermissions(@RequestBody CheckPermissionRequestDto requestBody) {
        return permissionsService.checkPermissions(requestBody.toCheckPermissionRequest())
                .thenApply(x -> {
                    if (x.getPermissionship() == CheckPermissionResponse.Permissionship.PERMISSIONSHIP_HAS_PERMISSION) {
                        return ResponseEntity.ok(x.getPermissionship().name());
                    } else {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(x.getPermissionship().name());
                    }
                });
    }

    @PostMapping("/permissions/expand")
    public CompletableFuture<ResponseEntity<ExpandPermissionTreeResponse>> expandPermissions(@RequestBody ExpandPermissionTreeRequest requestBody) {
        return permissionsService.expandPermissions(requestBody)
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/permissions/resources")
    public CompletableFuture<ResponseEntity<List<LookupResourcesResponseDto>>> lookupResources(@RequestBody LookupResourcesRequestDto requestBody) {
        return permissionsService.lookupResources(requestBody.toLookupResourcesRequest())
                .thenApply(list -> ResponseEntity.ok(LookupResourcesResponseDto.fromList(list)));
    }

    @PostMapping("/permissions/subjects")
    public CompletableFuture<ResponseEntity<List<LookupSubjectsResponseDto>>> lookupSubjects(@RequestBody LookupSubjectsRequestDto requestBody) {
        return permissionsService.lookupSubjects(requestBody.toLookupSubjectsRequest())
                .thenApply(list -> ResponseEntity.ok(LookupSubjectsResponseDto.fromList(list)));
    }
}