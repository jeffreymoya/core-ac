package com.synpulse8.pulse8.core.accesscontrolsvc.controller;

import com.authzed.api.v1.PermissionService.*;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.CheckPermissionRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PermissionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(value = "/v1", consumes = "application/json", produces = "application/json")
public class PermissionsController {

    private final PermissionsService permissionsService;

    @Autowired
    public PermissionsController(PermissionsService permissionsService) {
        this.permissionsService = permissionsService;
    }

    @PostMapping("/relationships/write")
    public CompletableFuture<ResponseEntity<WriteRelationshipsResponse>> writeRelationships(@RequestBody WriteRelationshipsRequest requestBody) {
        return permissionsService.writeRelationships(requestBody)
                .thenApply(ResponseEntity::ok);
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
    public CompletableFuture<ResponseEntity<CheckPermissionResponse>> checkPermissions(@RequestBody CheckPermissionRequestDto requestBody) {
        return permissionsService.checkPermissions(requestBody.toCheckPermissionRequest())
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/permissions/expand")
    public CompletableFuture<ResponseEntity<ExpandPermissionTreeResponse>> expandPermissions(@RequestBody ExpandPermissionTreeRequest requestBody) {
        return permissionsService.expandPermissions(requestBody)
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/permissions/resources")
    public CompletableFuture<ResponseEntity<Iterator<LookupResourcesResponse>>> lookupResources(@RequestBody LookupResourcesRequest requestBody) {
        return permissionsService.lookupResources(requestBody)
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/permissions/subjects")
    public CompletableFuture<ResponseEntity<Iterator<LookupSubjectsResponse>>> lookupSubjects(@RequestBody LookupSubjectsRequest requestBody) {
        return permissionsService.lookupSubjects(requestBody)
                .thenApply(ResponseEntity::ok);
    }
}