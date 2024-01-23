package com.synpulse8.pulse8.core.accesscontrolsvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.EditRoleDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.RolesAndPermissionDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.WriteRelationshipRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.WriteSchemaRequestDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PermissionsService;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.SchemaService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.ValidatableResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;

public class RoleSteps extends StepDefinitionBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleSteps.class);

    public RoleSteps(SchemaService schemaService, PermissionsService permissionsService, ObjectMapper objectMapper) {
        super(schemaService, permissionsService, objectMapper);
    }

    @Before
    public void setUp() throws InterruptedException {
        super.setUp();
    }

    @Given("a user edits a role with principal {string}")
    public void aUserEditsARoleWithPrincipal(String principal) throws JsonProcessingException {
        JsonNode testNode = testInput.path("edit_role");
        EditRoleDto editRoleDto = objectMapper.convertValue(testNode, EditRoleDto.class);

        response = given()
                .header(principalHeader, principal)
                .contentType("application/json")
                .body(editRoleDto)
                .put("v1/roles");
    }

    @Then("the response should be HTTP {int}")
    public void theResponseCodeShouldBe(int statusCode) {
        ValidatableResponse then = response.then();
        then.statusCode(statusCode);
    }

    @Given("the response should contain the edited role in the schema")
    public void theResponseShouldContainTheEditedRoleInTheSchema() throws IOException {
        JsonNode testNode = testInput.path("edit_role");
        EditRoleDto editRoleDto = objectMapper.convertValue(testNode, EditRoleDto.class);

        String expected = "relation " + editRoleDto.getUpdatedRoleName() + ": " + String.join(" | ", editRoleDto.getSubjects());

        given().header(principalHeader, "1234").when().get("/v1/schema")
                .then().assertThat().body(containsString(expected));
    }


    @Given("the {string} role is written in the resource {string}")
    public void aRoleIsWritten(String roleName, String resourceName) throws InterruptedException {
        JsonNode testNode = testInput.path("schema").path("update").path(resourceName).path(roleName);
        WriteSchemaRequestDto requestBody = objectMapper.convertValue(Collections.singletonMap("schema", testNode), WriteSchemaRequestDto.class);
        schemaService.writeSchema(requestBody.toWriteSchemaRequest()).join();
        sleep(updateSchemaToken);
    }

    @Given("the {string} role is written in the resource {string} and has relationships")
    public void aRoleIsWrittenAndHasRelationships(String roleName, String resourceName) throws InterruptedException {
        aRoleIsWritten(roleName, resourceName);
        WriteRelationshipRequestDto request = objectMapper.convertValue(testInput.get("relationships").get("create").get(roleName), WriteRelationshipRequestDto.class);
        permissionsService.writeRelationships(request.toWriteRelationshipRequest())
                .thenAccept(r -> writeRelationshipToken.set(r.getWrittenAt().getToken()));
        sleep(writeRelationshipToken);
    }

    @When("a user deletes {string} role under {string} policy using principal {string}")
    public void aUserDeletesRoleWithPrincipal(String roleName, String resourceName, String principal) throws JsonProcessingException, InterruptedException {
        String url = "/v1/roles/" + resourceName + "/" + roleName;

        response = given()
                .header(principalHeader, principal)
                .when()
                .delete(url);
    }

    @When("user wants to view roles of user with subject reference id {string} and subject reference type {string} from object type {string}")
    public void userWantsToViewRolesOfUserWithSubjectReferenceIdAndSubjectReferenceTypeFromObjectType(String subjRefObjId, String subjRefObjType, String objectType) {
        response = given()
                .header(principalHeader, "test-user")
                .param("objectType", objectType)
                .param("subjRefObjId", subjRefObjId)
                .param("subjRefObjType", subjRefObjType)
                .when()
                .get("/v1/roles");
    }

    @Then("the response should contain the list of roles and permissions associated to it")
    public void theResponseContainListOfRolesAndPermissionAssociated(){
        JsonNode rolesAndPermissions = testInput.path("rolesandpermission");
        RolesAndPermissionDto rolesAndPermissionDto = objectMapper.convertValue(rolesAndPermissions, RolesAndPermissionDto.class);
        rolesAndPermissionDto.getRoles().stream().forEach( entry ->
                assertEquals(entry, rolesAndPermissionDto.getRoles().get(0)));
    }

    @Then("the response code should be HTTP {int}")
    public void theResponseCodeShouldBeHTTP(int statusCode) {
        ValidatableResponse then = response.then();
        then.statusCode(statusCode);
    }
}