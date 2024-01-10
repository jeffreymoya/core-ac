package com.synpulse8.pulse8.core.accesscontrolsvc.service;

import com.authzed.api.v1.SchemaServiceOuterClass;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public interface SchemaService {
    CompletableFuture<SchemaServiceOuterClass.ReadSchemaResponse> readSchema(SchemaServiceOuterClass.ReadSchemaRequest request);

    CompletableFuture<SchemaServiceOuterClass.WriteSchemaResponse> writeSchema(SchemaServiceOuterClass.WriteSchemaRequest request);

    CompletableFuture<SchemaServiceOuterClass.WriteSchemaResponse> writeSchema(String schema);

}
