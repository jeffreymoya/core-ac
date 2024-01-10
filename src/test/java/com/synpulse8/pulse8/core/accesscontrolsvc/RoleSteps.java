package com.synpulse8.pulse8.core.accesscontrolsvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.EditRoleDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PermissionsService;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.SchemaService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.response.ValidatableResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

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

}