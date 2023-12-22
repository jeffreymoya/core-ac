package com.synpulse8.pulse8.core.accesscontrolsvc.service;

import com.authzed.api.v1.PermissionService.*;
import com.authzed.api.v1.PermissionsServiceGrpc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.CheckPermissionRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.models.AccountRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@Service
public class PermissionsServiceImpl implements PermissionsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsServiceImpl.class);
    private final PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsService;
    private final ObjectMapper objectMapper;

    @Autowired
    public PermissionsServiceImpl(PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsService, ObjectMapper objectMapper) {
        this.permissionsService = permissionsService;
        this.objectMapper = objectMapper;
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

    @Override
    public CompletableFuture<Boolean> bulkCheckPermissions(CheckPermissionRequestDto dto, String roles) throws JsonProcessingException {
        AccountRoles accountRoles = objectMapper.readValue(roles, AccountRoles.class);
        CompletableFuture<Boolean> responseFuture = new CompletableFuture<>();
        AtomicBoolean found = new AtomicBoolean(false);
        ExecutorService executor = Executors.newFixedThreadPool(10);

        List<CompletableFuture<Void>> futures = accountRoles.getAccount().getRoles().stream().map(role -> {
            CheckPermissionRequestDto request = CheckPermissionRequestDto.builder()
                    .permission(dto.getPermission())
                    .subjRefObjId(dto.getSubjRefObjId())
                    .subjRefObjType(role)
                    .subjRelation(dto.getSubjRelation())
                    .objectType(dto.getObjectType())
                    .objectId(dto.getObjectId())
                    .build();
            return CompletableFuture.runAsync(() -> {
                try {
                    CheckPermissionResponse c = permissionsService.checkPermission(request.toCheckPermissionRequest());
                    if (c.getPermissionship().equals(CheckPermissionResponse.Permissionship.PERMISSIONSHIP_HAS_PERMISSION) && !found.get()) {
                        found.set(true);
                        responseFuture.complete(true);
                    }
                } catch (Exception e) {
                    responseFuture.completeExceptionally(e);
                    throw new RuntimeException("Error while processing request", e);
                }
            }, executor);
        }).toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).whenComplete((v, ex) -> {
            executor.shutdown();
            if (!found.get() && !responseFuture.isCompletedExceptionally()) {
                responseFuture.complete(false);
            }
        });

        return responseFuture;
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

