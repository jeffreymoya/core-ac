package com.synpulse8.pulse8.core.accesscontrolsvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpulse8.pulse8.core.accesscontrolsvc.enums.HttpMethodPermission;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.P8CKafkaTopic;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PermissionsService;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.SchemaService;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

import static org.junit.Assert.assertTrue;

public class AuditRoleControllerSteps extends StepDefinitionBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditRoleControllerSteps.class);

    private final AuditLogConsumerTest consumer;

    public AuditRoleControllerSteps(SchemaService schemaService, PermissionsService permissionsService, ObjectMapper objectMapper, AuditLogConsumerTest consumer) {
        super(schemaService, permissionsService, objectMapper);
        this.consumer = consumer;
    }

    @Before
    public void setUp() throws InterruptedException {
        super.setUp();
        System.setProperty("spring.kafka.bootstrap-servers", PermissionsIntegrationTest.kafka.getBootstrapServers());
        LOGGER.info("Kafka bootstrap servers: {}", PermissionsIntegrationTest.kafka.getBootstrapServers());
    }

    @Given("Api and Kafka are available")
    public void iAmAnAuthenticatedUser() {
        assertTrue(PermissionsIntegrationTest.spicedb.isRunning());
        assertTrue(PermissionsIntegrationTest.kafka.isRunning());
    }

    @When("I send a POST request to {string} with the following JSON body:")
    public void iSendAPostRequestToWithTheFollowingJsonBody(String path, String jsonBody) {
    }

    @Then("the attribute log should contain a message with the attribute topic")
    public void theAttributeLogShouldContainAMessageWithTheTopic() throws InterruptedException {
        boolean messageConsumed = consumer.getAttributeLatch().await(15, TimeUnit.SECONDS);
        assertTrue(messageConsumed);
        assertTrue("Log contains the expected topic", consumer.getAttributesAuditLog().contains(P8CKafkaTopic.LOGS_ATTRIBUTES));
    }

    @Then("the role log should contain a message with the role topic")
    public void theRoleLogShouldContainAMessageWithTheTopic() throws InterruptedException {
        boolean messageConsumed = consumer.getRoleLatch().await(10, TimeUnit.SECONDS);
        assertTrue(messageConsumed);
        assertTrue("Log contains the expected topic", consumer.getRolesAuditLog().contains(P8CKafkaTopic.LOGS_ROLES));
    }

    @And("the attribute log should contain a message with the relationship topic")
    public void theAttributeLogShouldContainAMessageWithTheRelationshipTopic() throws InterruptedException {
        boolean messageConsumed = consumer.getRelationshipLatch().await(10, TimeUnit.SECONDS);
        assertTrue(messageConsumed);
        assertTrue("Log contains the expected topic", consumer.getRelationshipsAuditLog().contains(P8CKafkaTopic.LOGS_RELATIONSHIPS));
    }
}
