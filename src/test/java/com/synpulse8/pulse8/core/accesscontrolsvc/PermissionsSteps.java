package com.synpulse8.pulse8.core.accesscontrolsvc;

import com.authzed.api.v1.SchemaServiceOuterClass;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.CheckPermissionRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.ReadRelationshipResponseDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.WriteRelationshipRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.WriteSchemaRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PermissionsService;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.SchemaService;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PermissionsSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsSteps.class);

    @LocalServerPort
    private int port;

    @Value("${p8c.security.principal-header}")
    private String principalHeader;

    private Response response;

    private JsonNode testInput;

    private SchemaService schemaService;

    private PermissionsService permissionsService;

    private ObjectMapper objectMapper;

    private static boolean initialSetup = true;

    private  static JsonNode schemaFile;

    static {
        try {
            ClassPathResource resource = new ClassPathResource("schema/schema_pbac_test_input.json");
            File file = resource.getFile();
            schemaFile = new ObjectMapper().readTree(file);
        } catch (IOException e) {
            LOGGER.error("Error while reading schema file", e);
        }
    }

    @Autowired
    public PermissionsSteps(SchemaService schemaService, PermissionsService permissionsService, ObjectMapper objectMapper) {
        this.schemaService = schemaService;
        this.permissionsService = permissionsService;
        this.objectMapper = objectMapper;
    }

    @Before
    public void setUp() throws IOException, InterruptedException {
        if(initialSetup) {
            RestAssured.baseURI = "http://localhost";
            RestAssured.port = port;
            RestAssured.defaultParser = Parser.JSON;
            ClassPathResource resource = new ClassPathResource("schema/schema_pbac_test_input.json");
            File file = resource.getFile();
            testInput = objectMapper.readTree(file);
            JsonNode testNode = testInput.path("schema").path("write");
            Map schema = new HashMap();
            schema.put("schema", testNode);
            WriteSchemaRequestDto requestBody = objectMapper.convertValue(schema, WriteSchemaRequestDto.class);
            SchemaServiceOuterClass.WriteSchemaResponse join = schemaService.writeSchema(requestBody.toWriteSchemaRequest()).join();
            testNode = testInput.path("relationships").path("create").path("initial");
            WriteRelationshipRequestDto request = objectMapper.convertValue(testNode, WriteRelationshipRequestDto.class);
            permissionsService.writeRelationships(request.toWriteRelationshipRequest()).join();
            // fix intermittent issue where api fails due to schema/relationships not being ready
            Thread.sleep(2000);
            initialSetup = false;
        } else {
            testInput = schemaFile;
        }
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

    @When("a user do a {string} lookup with principal {string}")
    public void aUserDoALookupWithPrincipal(String type, String principal) throws JsonProcessingException, ExecutionException, InterruptedException {
        JsonNode testNode = testInput.path("lookup")
                .path(type)
                .path("request");
        Map<String, Object> requestBody = objectMapper.convertValue(testNode, new TypeReference<>() {});
        final RequestSpecification builder = given()
                .contentType("application/json")
                .body(objectMapper.writeValueAsString(requestBody));

        if(principal != null && !principal.isEmpty()) {
            builder.header(principalHeader, principal);
        }

        response = builder.when().post("/v1/permissions/" + type);
    }

    @And("the response body should contain the {string} data")
    public void theResponseBodyShouldContainTheData(String type) throws ExecutionException, InterruptedException {
        JsonNode expectedResponse = testInput.path("lookup").path(type).path("response");
        response.then().assertThat().body("size()", equalTo(1));
        List<Object> receivedResponse = response.getBody().jsonPath().getList("");
        String id = type.substring(0, type.length() - 1) + "ObjectId";
        JsonNode receivedNode = objectMapper.convertValue(receivedResponse.get(0), JsonNode.class);
        JsonNode expectedNode = expectedResponse.get(0);
        assertEquals(expectedNode.get("permission").asInt(), receivedNode.get("permission").asInt());
        assertEquals(expectedNode.get(id).asText(), receivedNode.get(id).asText());

    }

    @And("the {string} relationships are written")
    public void theRelationshipsAreWritten(String relation) throws InterruptedException {
        JsonNode testNode = testInput.path("relationships")
                .path("update")
                .path(relation);
        WriteRelationshipRequestDto request = objectMapper.convertValue(testNode, WriteRelationshipRequestDto.class);
        permissionsService.writeRelationships(request.toWriteRelationshipRequest()).join();
        // fix intermittent issue where api fails due to schema/relationships not being ready
        Thread.sleep(2000);
    }

    @When("a user reads {string} relationships with principal {string}")
    public void aUserReadsRelationshipsWithPrincipal(String relation, String principal) throws JsonProcessingException {
        JsonNode testNode = testInput.path("relationships")
                .path("view")
                .path(relation)
                .path("request");
        final RequestSpecification builder = createRequestSpecificationBuilder(testNode, principal);
        response = builder.when().post("/v1/relationships/read");
    }

    @When("a user deletes {string} relationships by {string} with principal {string}")
    public void aUserDeletesRelationshipsWithPrincipal(String relation, String option, String principal) throws JsonProcessingException, InterruptedException {
        JsonNode testNode = testInput.path("relationships")
                .path("delete")
                .path(relation)
                .path(option);

        final RequestSpecification builder = createRequestSpecificationBuilder(testNode, principal);
        String path = "/v1/relationships/" + (option.equals("filter") ? "delete" : "write");
        response = builder.when().post(path);

        // fix intermittent issue where api fails due to schema/relationships not being ready
        Thread.sleep(2000);
    }

    @And("the response body should contain the {string} relationship list")
    public void theResponseBodyShouldContainTheRelationshipList(String relation) {
        JsonNode expectedNode = testInput.path("relationships")
                .path("view")
                .path(relation)
                .path("response");
        theResponseBodyShouldContainTheListSize(1);
        List<Object> receivedResponse = response.getBody().jsonPath().getList("");
        JsonNode receivedNode = objectMapper.convertValue(receivedResponse, JsonNode.class);
        Field[] fields = ReadRelationshipResponseDto.class.getSuperclass().getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            assertEquals(expectedNode.get(0).get(fieldName), receivedNode.get(0).get(fieldName));
        }
    }

    @And("the response body should contain the list size {int}")
    public void theResponseBodyShouldContainTheListSize(int size) {
        response.then().assertThat().body("size()", equalTo(size));
    }

    private RequestSpecification createRequestSpecificationBuilder(JsonNode testNode, String principal) throws JsonProcessingException {
        Map<String, Object> requestBody = objectMapper.convertValue(testNode, new TypeReference<>() {});
        final RequestSpecification builder = given()
                .contentType("application/json")
                .body(objectMapper.writeValueAsString(requestBody));

        if(principal != null && !principal.isEmpty()) {
            builder.header(principalHeader, principal);
        }

        return builder;
    }
}