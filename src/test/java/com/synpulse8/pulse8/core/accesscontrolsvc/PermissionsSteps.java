package com.synpulse8.pulse8.core.accesscontrolsvc;

import com.authzed.api.v1.PermissionService;
import com.authzed.api.v1.SchemaServiceOuterClass;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.CheckPermissionRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.WriteRelationshipRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.WriteSchemaRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PermissionsService;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.SchemaService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PermissionsSteps {

    @LocalServerPort
    private int port;

    @Value("${p8c.security.principal-header}")
    private String principalHeader;

    private Response response;

    ObjectMapper objectMapper;

    private JsonNode testInput;

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private PermissionsService permissionsService;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.defaultParser = Parser.JSON;
    }
    @Given("the API is available")
    public void theApiIsAvailable() {
        assertTrue(PermissionsIntegrationTest.spicedb.isRunning());
    }

    @When("a user checks permissions with principal {string}")
    public void aUserChecksTheDocument(String principal) throws JsonProcessingException {
        CheckPermissionRequestDto dto = CheckPermissionRequestDto.builder()
                .objectType("blog/post")
                .objectId("1")
                .subjRefObjType("blog/user")
                .subjRefObjId("emilia")
                .permission("read")
                .build();
        RequestSpecification builder = given()
                .contentType("application/json");

        if(principal != null && !principal.isEmpty()) {
            builder.header(principalHeader, principal);
        }

        response = builder
                .body(objectMapper.writeValueAsString(dto))
                .when()
                .post("/v1/permissions/check");
    }

    @Then("the response code should be {int}")
    public void theResponseCodeShouldBe(int statusCode) {
        ValidatableResponse then = response.then();
        then.statusCode(statusCode);
    }

    @Then("the response should contain {string}")
    public void theResponseShouldContain(String expectedResponse) {
        response.then().body("error", equalTo(expectedResponse));
    }

    @Before("@ReadTestInput")
    public void readTestInput() throws IOException {
        ClassPathResource resource = new ClassPathResource("schema/schema_pbac_test_input.json");
        File file = resource.getFile();
        objectMapper = new ObjectMapper();
        testInput = objectMapper.readTree(file);
    }

    @Given("the schema is written")
    public void theSchemaIsWritten() throws IOException, ExecutionException, InterruptedException {
        JsonNode testNode = testInput.path("writeSchema").path("schema");
        Map schema = new HashMap();
        schema.put("schema", testNode);
        WriteSchemaRequestDto requestBody = objectMapper.convertValue(schema, WriteSchemaRequestDto.class);
        CompletableFuture<SchemaServiceOuterClass.WriteSchemaResponse> writeSchemaResponse = schemaService.writeSchema(requestBody.toWriteSchemaRequest());
        assertNotNull(writeSchemaResponse.get().getWrittenAt());
    }

    @Given("the relationships are written")
    public void theRelationshipsAreWritten() {
        WriteRelationshipRequestDto request = objectMapper.convertValue(testInput.get("createRelationships"), WriteRelationshipRequestDto.class);
        CompletableFuture<PermissionService.WriteRelationshipsResponse> writeRelationshipResponse = permissionsService.writeRelationships(request.toWriteRelationshipRequest());
        writeRelationshipResponse.join();
    }

    @When("a user checks PBAC {string} permission of {string} with principal {string}")
    public void aUserChecksPBACPermission(String permissionName, String subjRefObjId, String principal) throws IOException {
        JsonNode testNode = testInput.path("checkPermission")
                .path(subjRefObjId)
                .path(permissionName);
        Map<String, Object> requestBody = objectMapper.convertValue(testNode, new TypeReference<>() {});
        RequestSpecification builder = given()
                .contentType("application/json");

        if(principal != null && !principal.isEmpty()) {
            builder.header(principalHeader, principal);
        }

        response = builder
                .body(objectMapper.writeValueAsString(requestBody))
                .when()
                .post("/v1/permissions/check");

    }
}