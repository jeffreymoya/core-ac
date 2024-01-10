package com.synpulse8.pulse8.core.accesscontrolsvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.WriteRelationshipRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.WriteSchemaRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PermissionsService;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.SchemaService;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

public class StepDefinitionBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(StepDefinitionBase.class);

    @LocalServerPort
    protected int port;

    @Value("${p8c.security.principal-header}")
    protected String principalHeader;

    protected Response response;

    protected static JsonNode testInput;

    protected SchemaService schemaService;

    protected PermissionsService permissionsService;

    protected ObjectMapper objectMapper;

    protected static boolean initialSetup = true;

    protected static final AtomicReference<String> writeRelationshipToken = new AtomicReference<>();

    protected static final AtomicReference<String> deleteRelationshipToken = new AtomicReference<>();


    static {
        try {
            ClassPathResource resource = new ClassPathResource("schema/schema_pbac_test_input.json");
            File file = resource.getFile();
            testInput = new ObjectMapper().readTree(file);
        } catch (IOException e) {
            LOGGER.error("Error while reading schema file", e);
        }
    }

    public StepDefinitionBase(SchemaService schemaService, PermissionsService permissionsService, ObjectMapper objectMapper) {
        this.schemaService = schemaService;
        this.permissionsService = permissionsService;
        this.objectMapper = objectMapper;
    }

    protected void setUp() throws InterruptedException {
        if(initialSetup) {
            RestAssured.baseURI = "http://localhost";
            RestAssured.port = port;
            RestAssured.defaultParser = Parser.JSON;
            JsonNode testNode = testInput.path("schema").path("write");
            WriteSchemaRequestDto requestBody = objectMapper.convertValue(Collections.singletonMap("schema", testNode), WriteSchemaRequestDto.class);
            schemaService.writeSchema(requestBody.toWriteSchemaRequest()).join();
            WriteRelationshipRequestDto request = objectMapper.convertValue(testInput.get("relationships").get("create").get("initial"), WriteRelationshipRequestDto.class);
            permissionsService.writeRelationships(request.toWriteRelationshipRequest())
                    .thenAccept(r -> writeRelationshipToken.set(r.getWrittenAt().getToken()));
            sleep(writeRelationshipToken);
            initialSetup = false;
        }
    }

    protected void sleep(AtomicReference<String> token) throws InterruptedException {
        long timeoutMillis = 10000; // 10 seconds
        long pollingIntervalMillis = 3000; // 3 second
        long startTime = System.currentTimeMillis();
        do {
            LOGGER.debug("Waiting for write/delete relationship to complete");
            Thread.sleep(pollingIntervalMillis);
        } while (token.get() == null && System.currentTimeMillis() - startTime < timeoutMillis);
    }
}
