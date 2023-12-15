package com.synpulse8.pulse8.core.accesscontrolsvc;

import com.authzed.api.v1.SchemaServiceOuterClass;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.*;
import com.synpulse8.pulse8.core.accesscontrolsvc.models.PolicyRolesAndPermissions;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasLength;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PermissionsSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsSteps.class);

    @LocalServerPort
    private int port;

    @Value("${p8c.security.principal-header}")
    private String principalHeader;

    private Response response;

    private static JsonNode testInput;

    private SchemaService schemaService;

    private PermissionsService permissionsService;

    private ObjectMapper objectMapper;

    private static boolean initialSetup = true;

    private static final AtomicReference<String> writeRelationshipToken = new AtomicReference<>();;


    static {
        try {
            ClassPathResource resource = new ClassPathResource("schema/schema_pbac_test_input.json");
            File file = resource.getFile();
            testInput = new ObjectMapper().readTree(file);
        } catch (IOException e) {
            LOGGER.error("Error while reading schema file", e);
        }
    }

    public PermissionsSteps(SchemaService schemaService, PermissionsService permissionsService, ObjectMapper objectMapper) {
        this.schemaService = schemaService;
        this.permissionsService = permissionsService;
        this.objectMapper = objectMapper;
    }

    @Before
    public void setUp() throws InterruptedException {
        if(initialSetup) {
            RestAssured.baseURI = "http://localhost";
            RestAssured.port = port;
            RestAssured.defaultParser = Parser.JSON;
            JsonNode testNode = testInput.path("writeSchema").path("schema");
            WriteSchemaRequestDto requestBody = objectMapper.convertValue(Collections.singletonMap("schema", testNode), WriteSchemaRequestDto.class);
            schemaService.writeSchema(requestBody.toWriteSchemaRequest()).join();
            WriteRelationshipRequestDto request = objectMapper.convertValue(testInput.get("createRelationships"), WriteRelationshipRequestDto.class);
            permissionsService.writeRelationships(request.toWriteRelationshipRequest())
                    .thenAccept(r -> writeRelationshipToken.set(r.getWrittenAt().getToken()));
            long timeoutMillis = 10000; // 10 seconds
            long pollingIntervalMillis = 3000; // 3 second
            long startTime = System.currentTimeMillis();
            do {
                LOGGER.debug("Waiting for write relationship to complete");
                Thread.sleep(pollingIntervalMillis);
            } while (writeRelationshipToken.get() == null && System.currentTimeMillis() - startTime < timeoutMillis);
            initialSetup = false;
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
        requestBody.put("atExactSnapshot", writeRelationshipToken.get());
        requestBody.put("fullyConsistent", true);

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

    @When("a user saves a policy definition with valid data")
    public void aUserSavesAPolicyDefinitionWithValidData() throws JsonProcessingException {
        JsonNode policy = testInput.path("policy");
        PolicyDefinitionDto dto = objectMapper.convertValue(policy, PolicyDefinitionDto.class);

        RequestSpecification builder = given()
                .contentType("application/json")
                .body(objectMapper.writeValueAsString(dto));

        response = builder
                .header(principalHeader, "test-user")
                .when()
                .post("/v1/policy");
    }

    @Then("the response should contain the policy ID")
    public void theResponseShouldContainThePolicyID() {
        // written_at length is 24
        response.then().assertThat().body(hasLength(24));
    }

    @When("a user gets all policy definitions")
    public void aUserGetsAllPolicyDefinitions() {
        response = given()
                .header(principalHeader, "test-user")
                .when()
                .get("/v1/policies");
    }

    @Then("the response should contain a list of policy definitions")
    public void theResponseShouldContainAListOfPolicyDefinitions() {
        JsonNode policy = testInput.path("policy");
        PolicyDefinitionDto dto = objectMapper.convertValue(policy, PolicyDefinitionDto.class);
        response.thenReturn().body().jsonPath().getList("", PolicyDefinitionDto.class).forEach(policyDefinitionDto -> {
            if(policyDefinitionDto.getName().equals(dto.getName())) {
                assertEquals(dto.getDescription(), policyDefinitionDto.getDescription());
                assertEquals(dto.getPermissions().size(), policyDefinitionDto.getPermissions().size());
                for (int i = 0; i < dto.getPermissions().size(); i++) {
                    PolicyRolesAndPermissions.Permission expected = dto.getPermissions().get(i);
                    PolicyRolesAndPermissions.Permission actual = policyDefinitionDto.getPermissions().get(i);
                    assertEquals(expected.getName(), actual.getName());
                    expected.getRolesOr().sort(String::compareTo);
                    actual.getRolesOr().sort(String::compareTo);
                    assertEquals(expected.getRolesOr(), actual.getRolesOr());
                }
                for (int i = 0; i < dto.getRoles().size(); i++) {
                    PolicyRolesAndPermissions.Role expected = dto.getRoles().get(i);
                    PolicyRolesAndPermissions.Role actual = policyDefinitionDto.getRoles().get(i);
                    assertEquals(expected.getName(), actual.getName());
                    expected.getSubjects().sort(String::compareTo);
                    actual.getSubjects().sort(String::compareTo);
                    assertEquals(expected.getSubjects(), actual.getSubjects());
                }
                dto.getAttributes().forEach((key, value) -> assertEquals(value, policyDefinitionDto.getAttributes().get(key)));
                assertEquals(dto.getAccess(), policyDefinitionDto.getAccess());
            }
        });
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