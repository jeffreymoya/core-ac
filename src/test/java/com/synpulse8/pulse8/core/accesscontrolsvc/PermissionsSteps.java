package com.synpulse8.pulse8.core.accesscontrolsvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpulse8.pulse8.core.accesscontrolsvc.dto.CheckPermissionRequestDto;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class PermissionsSteps {

    @LocalServerPort
    private int port;

    private Response response;

    ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }
    @Given("the API is available")
    public void theApiIsAvailable() {

    }

    @When("a user checks permissions")
    public void aUserChecksTheDocument() throws JsonProcessingException {
        CheckPermissionRequestDto dto = CheckPermissionRequestDto.builder()
                .objectType("blog/post")
                .objectId("1")
                .subjRefObjType("blog/user")
                .subjRefObjId("emilia")
                .permission("read")
                .build();
        response = given()
                .contentType("application/json")
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
}