package com.synpulse8.pulse8.core.accesscontrolsvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.P8CKafkaTopic;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PermissionsService;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.SchemaService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class AuditRoleControllerSteps extends StepDefinitionBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditRoleControllerSteps.class);

    private final BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();

    public AuditRoleControllerSteps(SchemaService schemaService, PermissionsService permissionsService, ObjectMapper objectMapper) {
        super(schemaService, permissionsService, objectMapper);
    }

    @Before
    public void setUp() throws InterruptedException {
        super.setUp();
        System.setProperty("spring.kafka.bootstrap-servers", PermissionsIntegrationTest.kafka.getBootstrapServers());
        LOGGER.info("Kafka bootstrap servers: {}", PermissionsIntegrationTest.kafka.getBootstrapServers());
    }

    @KafkaListener(topics = P8CKafkaTopic.LOGS_ATTRIBUTES, groupId = "ac.audit.role", containerFactory = "kafkaListenerLog4jContainerFactory")
    public void listen(ConsumerRecord<String, String> record) {
        LOGGER.info("Received log4j message: {}", record.value());
        records.add(record);
    }

    @Given("Api and Kafka are available")
    public void iAmAnAuthenticatedUser() {
        assertTrue(PermissionsIntegrationTest.spicedb.isRunning());
        assertTrue(PermissionsIntegrationTest.kafka.isRunning());
    }

    @When("I send a POST request to {string} with the following JSON body:")
    public void iSendAPostRequestToWithTheFollowingJsonBody(String path, String jsonBody) {
    }

    @Then("the log should contain a message with the topic {string}")
    public void theLogShouldContainAMessageWithTheTopic(String topic) throws InterruptedException {
        ConsumerRecord<String, String> received = records.poll(10, TimeUnit.SECONDS);
        assertTrue("Log contains the expected topic", received.value().contains(topic));
    }

}
