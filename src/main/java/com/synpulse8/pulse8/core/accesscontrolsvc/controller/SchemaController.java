package com.synpulse8.pulse8.core.accesscontrolsvc.controller;

import com.authzed.api.v1.SchemaServiceOuterClass;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.WriteSchemaRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.SchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(value = "/v1", produces = "application/json")
public class SchemaController {

    private final SchemaService schemaService;

    @Autowired
    public SchemaController(SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @PostMapping("/schema/read")
    public CompletableFuture<ResponseEntity<String>> readSchema() {
        SchemaServiceOuterClass.ReadSchemaRequest requestBody = SchemaServiceOuterClass.ReadSchemaRequest
                .newBuilder()
                .build();
        return schemaService.readSchema(requestBody)
                .thenApply(x -> ResponseEntity.ok(x.getSchemaText()));
    }
    @PostMapping("/schema/write")
    public CompletableFuture<ResponseEntity<String>> writeSchema(@RequestBody WriteSchemaRequestDto requestBody) {
        return schemaService.writeSchema(requestBody.toWriteSchemaRequest())
        .thenApply(x -> ResponseEntity.ok(x.getWrittenAt().getToken()));
    }

}
