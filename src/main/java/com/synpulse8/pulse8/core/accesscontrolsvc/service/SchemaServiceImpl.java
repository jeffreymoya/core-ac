package com.synpulse8.pulse8.core.accesscontrolsvc.service;

import com.authzed.api.v1.SchemaServiceGrpc;
import com.authzed.api.v1.SchemaServiceOuterClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Service
public class SchemaServiceImpl implements SchemaService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaServiceImpl.class);
    private final SchemaServiceGrpc.SchemaServiceBlockingStub schemaService;

    @Autowired
    public SchemaServiceImpl(SchemaServiceGrpc.SchemaServiceBlockingStub schemaService) {
        this.schemaService = schemaService;
    }

    @Override
    public CompletableFuture<SchemaServiceOuterClass.ReadSchemaResponse> readSchema(SchemaServiceOuterClass.ReadSchemaRequest request) {
        return runAsyncTask(() -> schemaService.readSchema(request));
    }

    @Override
    public CompletableFuture<SchemaServiceOuterClass.WriteSchemaResponse> writeSchema(String schema) {
        return runAsyncTask(() -> schemaService.writeSchema(SchemaServiceOuterClass.WriteSchemaRequest.newBuilder().setSchema(schema).build()));
    }

    @Override
    public CompletableFuture<SchemaServiceOuterClass.WriteSchemaResponse> writeSchema(SchemaServiceOuterClass.WriteSchemaRequest request) {
        return runAsyncTask(() -> schemaService.writeSchema(request));
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