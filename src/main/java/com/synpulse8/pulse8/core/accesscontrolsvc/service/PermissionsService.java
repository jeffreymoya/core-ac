package com.synpulse8.pulse8.core.accesscontrolsvc.service;

import com.authzed.api.v1.PermissionService.CheckPermissionRequest;
import com.authzed.api.v1.PermissionService.CheckPermissionResponse;
import com.authzed.api.v1.PermissionService.ExpandPermissionTreeResponse;
import com.authzed.api.v1.PermissionService.ReadRelationshipsRequest;
import com.authzed.api.v1.PermissionService.ReadRelationshipsResponse;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.CheckPermissionRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.CheckRoutePermissionDto;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import static com.authzed.api.v1.PermissionService.*;

@Service
public interface PermissionsService {

    CompletableFuture<WriteRelationshipsResponse> writeRelationships(WriteRelationshipsRequest request);

    CompletableFuture<Iterator<ReadRelationshipsResponse>> readRelationships(ReadRelationshipsRequest request);

    CompletableFuture<DeleteRelationshipsResponse> deleteRelationships(DeleteRelationshipsRequest request);

    CompletableFuture<CheckPermissionResponse> checkPermissions(CheckPermissionRequest request);

    CompletableFuture<ExpandPermissionTreeResponse> expandPermissions(ExpandPermissionTreeRequest request);

    CompletableFuture<Iterator<LookupResourcesResponse>> lookupResources(LookupResourcesRequest request);

    CompletableFuture<Iterator<LookupSubjectsResponse>> lookupSubjects(LookupSubjectsRequest request);
}