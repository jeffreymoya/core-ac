package com.synpulse8.pulse8.core.accesscontrolsvc;

import com.authzed.api.v1.PermissionService;
import com.authzed.api.v1.SchemaServiceOuterClass;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.*;
import com.synpulse8.pulse8.core.accesscontrolsvc.enums.HttpMethodPermission;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.P8CException;
import com.synpulse8.pulse8.core.accesscontrolsvc.models.PolicyRolesAndPermissions;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PermissionsService;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.SchemaService;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class PermissionsSteps extends StepDefinitionBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsSteps.class);

    public PermissionsSteps(SchemaService schemaService, PermissionsService permissionsService, ObjectMapper objectMapper) {
        super(schemaService, permissionsService, objectMapper);
    }

    @Before
    public void setUp() throws InterruptedException {
        super.setUp();
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

    @Then("the error should contain {string}")
    public void theErrorShouldContain(String expectedResponse) {
        response.then().body("error", equalTo(expectedResponse));
    }

    @Then("the response should contain {string}")
    public void theResponseShouldContain(String expectedResponse) {
        response.then().assertThat().body(containsString(expectedResponse));
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


    @When("a user gets attribute from policy with name {string}")
    public void aUserGetsAttributeFromPolicyWithName(String policyName) {
        response = given()
                .header(principalHeader, "test-user")
                .when()
                .get("/v1/attributes/" + policyName);
    }

    @Then("the response body should contain a map of policy attributes")
    public void theResponseBodyShouldContainAMapOfPolicyAttributes(){
        JsonNode policy = testInput.path("policy");
        PolicyDefinitionDto dto = objectMapper.convertValue(policy, PolicyDefinitionDto.class);
        dto.getAttributes().entrySet().stream().forEach( entry ->
                assertEquals(dto.getAttributes().get(entry.getKey()), entry.getValue())
        );
    }

    @When("a user adds attribute to an existing policy")
    public void aUserAddsAttributeToAnExistingPolicy() throws P8CException, JsonProcessingException {
        JsonNode attributes = testInput.path("attribute");
        AttributeDefinitionDto attributeDefinitionDto = objectMapper.convertValue(attributes, AttributeDefinitionDto.class);

        RequestSpecification builder = given()
                .contentType("application/json")
                .body(objectMapper.writeValueAsString(attributeDefinitionDto));

        response = builder
                .header(principalHeader, "test-user")
                .when()
                .post("/v1/attributes");

    }

    @Then("the attribute response code should be {int}")
    public void theAttributeResponseCodeShouldBe(int statusCode) {
        ValidatableResponse then = response.then();
        then.statusCode(statusCode);
    }

    @And("the {string} relationships are written")
    public void theRelationshipsAreWritten(String relation) throws InterruptedException {
        JsonNode testNode = testInput.path("relationships")
                .path("update")
                .path(relation);
        writeRelationships(testNode);
    }

    @When("a user reads {string} relationships with principal {string}")
    public void aUserReadsRelationshipsWithPrincipal(String relation, String principal) {
        JsonNode testNode = testInput.path("relationships")
                .path("view")
                .path(relation)
                .path("request");
        final RequestSpecification builder = createRequestSpecificationBuilder(principal, HttpMethodPermission.GET);
        String url = "/v1/relationships" + createRequestQueryString(testNode);
        response = builder.when().get(url);
    }

    @When("a user deletes {string} relationships by {string} with principal {string}")
    public void aUserDeletesRelationshipsWithPrincipal(String relation, String option, String principal) {
        JsonNode testNode = testInput.path("relationships")
                .path("delete")
                .path(relation)
                .path(option);

        final RequestSpecification builder = createRequestSpecificationBuilder(principal, HttpMethodPermission.DELETE);
        String url = "/v1/relationships";
        if (option.equals("filter")) {
            url += createRequestQueryString(testNode);
        } else {
            url += "/{objectType}/{objectId}/{relation}/{subjRefObjType}/{subjRefObjId}";
            builder.pathParams(objectMapper.convertValue(testNode, new TypeReference<>() {}));
        }
        response = builder.when().delete(url);
    }

    @Then("the delete relationship response code should be {int}")
    public void theDeleteRelationshipResponseCodeShouldBe(int statusCode) throws InterruptedException {
        theRelationshipResponseCodeShouldBe(statusCode, deleteRelationshipToken);
    }

    @Then("the write relationship response code should be {int}")
    public void theWriteRelationshipResponseCodeShouldBe(int statusCode) throws InterruptedException {
        theRelationshipResponseCodeShouldBe(statusCode, writeRelationshipToken);
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

    @When("a user writes {string} relation and permission to the {string} resource with principal {string}")
    public void aUserUpdatesSchemaWithPrincipal(String relation, String resource, String principal) {
        JsonNode testNode = testInput.path("schema")
                .path("update")
                .path(resource)
                .path(relation);

        final RequestSpecification builder = createRequestSpecificationBuilder(principal, HttpMethodPermission.POST);

        response = builder
                .body(testNode.asText())
                .when()
                .post("/v1/schema");
    }

    @When("a user writes {string} relationship to the resource with principal {string}")
    public void aUserWritesRelationshipWithPrincipal(String relation, String principal) throws InterruptedException {
        JsonNode testNode = testInput.path("relationships")
                .path("create")
                .path(relation);
        WriteRelationshipRequestDto requestBody = objectMapper.convertValue(testNode, WriteRelationshipRequestDto.class);

        final RequestSpecification builder = createRequestSpecificationBuilder(principal, HttpMethodPermission.POST);

        response = builder
                .body(requestBody)
                .when()
                .post("/v1/relationships");
        sleep(writeRelationshipToken);
    }

    RequestSpecification createRequestSpecificationBuilder(String principal, HttpMethodPermission httpMethodPermission) {
        final RequestSpecification builder = given();

        if (httpMethodPermission.equals(HttpMethodPermission.POST)) {
            builder.contentType("application/json");
        }

        if(principal != null && !principal.isEmpty()) {
            builder.header(principalHeader, principal);
        }

        return builder;
    }

    String createRequestQueryString(JsonNode testNode) {
        Map<String, Object> queryParams = objectMapper.convertValue(testNode, new TypeReference<>() {});
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        queryParams.forEach(builder::queryParam);
        return builder.build().encode().toUriString();
    }


    @When("a user gets definition of policy with name {string}")
    public void aUserGetsDefinitionOfPolicyWithName(String policyName) {
        response = given()
                .header(principalHeader, "test-user")
                .when()
                .get("/v1/policies/" + policyName);
    }

    @Then("the response body should contain a map of policy definition")
    public void theResponseBodyShouldContainAMapOfPolicyDefinition(){
        JsonNode policy = testInput.path("policy");
        PolicyDefinitionDto dto = objectMapper.convertValue(policy, PolicyDefinitionDto.class);
        dto.getAttributes().entrySet().stream().forEach( entry ->
                assertEquals(dto.getAttributes().get(entry.getKey()), entry.getValue())
        );
    }

    @When("a user checks route permissions via {string} with principal {string} and route {string} and uriTemplate {string}")
    public void aUserChecksRoutePermissionsWithPrincipalAndRoute(String method, String principal, String route, String uriTemplate) {
        CheckRoutePermissionDto dto = CheckRoutePermissionDto.builder()
                .route(route)
                .method(HttpMethodPermission.valueOf(method))
                .uriTemplate(uriTemplate)
                .build();


        response = given()
                .header(principalHeader, principal)
                .contentType("application/json")
                .body(dto)
                .when()
                .post("/v1/permissions/route/check");
    }

    @When("a user checks route permissions via {string} with principal {string} and route {string} and object type {string}")
    public void aUserChecksRoutePermissionsViaWithPrincipalAndRouteAndObjectTypeAndObjectId(String method, String principal, String route, String objectType) {
        CheckRoutePermissionDto dto = CheckRoutePermissionDto.builder()
                .route(route)
                .objectType(objectType)
                .method(HttpMethodPermission.valueOf(method))
                .build();


        response = given()
                .header(principalHeader, principal)
                .contentType("application/json")
                .body(dto)
                .when()
                .post("/v1/permissions/route/check");
    }

    @And("the user {string} should have {string} permission to the related resource")
    public void theUserShouldHavePermissionToTheResource(String subjRefObjId, String permissionName) {
        JsonNode testNode = testInput.path("checkPermission").path(subjRefObjId).path(permissionName);
        CheckPermissionRequestDto requestBody = objectMapper.convertValue(testNode, new TypeReference<>() {});
        permissionsService.checkPermissions(requestBody.toCheckPermissionRequest())
                .thenAccept(r -> assertSame(r.getPermissionship(), PermissionService.CheckPermissionResponse.Permissionship.PERMISSIONSHIP_HAS_PERMISSION));
    }

    @After("@CaveatScenario")
    public void cleanupCaveatScenario() throws ExecutionException, InterruptedException {
        permissionsService.deleteRelationships(DeleteRelationshipRequestDto.builder().objectType("test_resource").build().toDeleteRelationshipsRequest()).get();
        permissionsService.deleteRelationships(DeleteRelationshipRequestDto.builder().objectType("test_organization").build().toDeleteRelationshipsRequest()).get();
        sleep(deleteRelationshipToken);
    }

    @Given("the {string} is written in the schema and has relationships")
    public void addPolicyToSchema(String policyName) throws InterruptedException, ExecutionException {
        SchemaServiceOuterClass.ReadSchemaRequest readSchemaRequestBody = SchemaServiceOuterClass.ReadSchemaRequest.newBuilder().build();
        SchemaServiceOuterClass.ReadSchemaResponse schemaResponse = schemaService.readSchema(readSchemaRequestBody).get();
        String schemaText = schemaResponse.getSchemaText() + testInput.get("schema").get(policyName).asText();
        updateSchema(schemaText);
        writeRelationships(testInput.get("relationships").get("create").get(policyName));
    }

    @When("a user checks permission of {string} with {string} {string} and principal {string}")
    public void aUserChecksCaveatPermission(String subjRefObjId, String scenario, String policy, String principal) throws IOException {
        JsonNode baseNode = testInput.path("checkPermission").path(subjRefObjId);
        Map<String, Object> requestBody = objectMapper.convertValue(baseNode.path("base"), new TypeReference<>() {});
        requestBody.putAll(objectMapper.convertValue(baseNode.path(policy).path(scenario), new TypeReference<>() {}));
        RequestSpecification builder = createRequestSpecificationBuilder(principal, HttpMethodPermission.POST);

        response = builder
                .body(objectMapper.writeValueAsString(requestBody))
                .when()
                .post("/v1/permissions/check");
    }
}