package com.synpulse8.pulse8.core.accesscontrolsvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.UpdatePolicyDto;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PermissionsService;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.SchemaService;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

public class PolicySteps extends StepDefinitionBase {

    public PolicySteps(SchemaService schemaService, PermissionsService permissionsService, ObjectMapper objectMapper) {
        super(schemaService, permissionsService, objectMapper);
    }

    @Before
    public void setUp() throws InterruptedException {
        super.setUp();
    }

    @Then("the response should be HTTP {int} and no errors")
    public void theResponseCodeShouldBe(int statusCode) {
        ValidatableResponse then = response.then();
        then.statusCode(statusCode);
    }
    @When("a user edits a policy with correct input")
    public void aUserEditsAPolicyWithPrincipal() {
        response = given()
                .header(principalHeader, "1234")
                .contentType("application/json")
                .body(createUpdatePolicyDto())
                .put("v1/policy");
    }

    private UpdatePolicyDto createUpdatePolicyDto() {
        JsonNode testNode = testInput.path("edit_policy");
        return objectMapper.convertValue(testNode, UpdatePolicyDto.class);
    }

    @When("a user edits a policy with a non-existent policy name")
    public void aUserEditsAPolicyWithANonExistentPolicyName() {
        UpdatePolicyDto updatePolicyDto = createUpdatePolicyDto();
        updatePolicyDto.setName("non-existent-policy");
        response = given()
                .header(principalHeader, "1234")
                .contentType("application/json")
                .body(updatePolicyDto)
                .put("v1/policy");
    }

    @And("the response should be HTTP {int} and the error message should contain {string}")
    public void theResponseShouldBeHTTPAndTheErrorMessageShouldStartWith(int status, String message) {
        ValidatableResponse then = response.then();
        then.statusCode(status);
        then.assertThat().body(containsString(message));
    }

    @When("a user edits a policy with a updated name that already exist")
    public void aUserEditsAPolicyWithAUpdatedNameThatAlreadyExist() {
        UpdatePolicyDto updatePolicyDto = createUpdatePolicyDto();
        updatePolicyDto.setUpdatedName("policy");
        response = given()
                .header(principalHeader, "1234")
                .contentType("application/json")
                .body(updatePolicyDto)
                .put("v1/policy");
    }

    @When("a user edits a policy with a non-existent role")
    public void aUserEditsAPolicyWithANonExistentRole() {
        UpdatePolicyDto updatePolicyDto = createUpdatePolicyDto();
        updatePolicyDto.setName("usergroup");
        updatePolicyDto.setUpdatedName("usergroup");
        response = given()
                .header(principalHeader, "1234")
                .contentType("application/json")
                .body(updatePolicyDto)
                .put("v1/policy");
    }
}