package com.synpulse8.pulse8.core.accesscontrolsvc.service;

import com.authzed.api.v1.PermissionService.*;
import com.authzed.api.v1.PermissionsServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Service
public class PermissionsServiceImpl implements PermissionsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsServiceImpl.class);
    private final PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsService;

    @Autowired
    public PermissionsServiceImpl(PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsService) {
        this.permissionsService = permissionsService;
    }

    @Override
    public CompletableFuture<WriteRelationshipsResponse> writeRelationships(WriteRelationshipsRequest request) {
        return runAsyncTask(() -> permissionsService.writeRelationships(request));
    }

    @Override
    public CompletableFuture<Iterator<ReadRelationshipsResponse>> readRelationships(ReadRelationshipsRequest request) {
        return runAsyncTask(() -> permissionsService.readRelationships(request));
    }

    @Override
    public CompletableFuture<DeleteRelationshipsResponse> deleteRelationships(DeleteRelationshipsRequest request) {
        return runAsyncTask(() -> permissionsService.deleteRelationships(request));
    }

    @Override
    public CompletableFuture<CheckPermissionResponse> checkPermissions(CheckPermissionRequest request) {
        return runAsyncTask(() -> permissionsService.checkPermission(request));
    }

    @Override
    public CompletableFuture<ExpandPermissionTreeResponse> expandPermissions(ExpandPermissionTreeRequest request) {
        return runAsyncTask(() -> permissionsService.expandPermissionTree(request));
    }

    @Override
    public CompletableFuture<Iterator<LookupResourcesResponse>> lookupResources(LookupResourcesRequest request) {
        return runAsyncTask(() -> permissionsService.lookupResources(request));
    }

    @Override
    public CompletableFuture<Iterator<LookupSubjectsResponse>> lookupSubjects(LookupSubjectsRequest request) {
        return runAsyncTask(() -> permissionsService.lookupSubjects(request));
    }

    private <T> CompletableFuture<T> runAsyncTask(Supplier<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.get();
            } catch (Exception e) {
                LOGGER.error("Error while processing request", e);
                throw new RuntimeException("Error while processing request", e);
            }
        });
    }
}

