package com.synpulse8.pulse8.core.accesscontrolsvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synpulse8.pulse8.core.RelationshipDeletion;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.DeleteRelationshipMessage;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.P8CKafkaTopic;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.consumer.DeleteRelationshipConsumer;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.consumer.KafkaConsumerService;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.producer.DeleteRelationshipProducer;
import com.synpulse8.pulse8.core.accesscontrolsvc.kafka.producer.KafkaProducerService;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.PermissionsService;
import com.synpulse8.pulse8.core.accesscontrolsvc.service.SchemaService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KafkaSteps extends StepDefinitionBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSteps.class);

    private final DeleteRelationshipConsumer consumer;

    private final KafkaTemplate<String, RelationshipDeletion> kafkaTemplate;

    private RelationshipDeletion deleteRelationshipMessage;

    public KafkaSteps(SchemaService schemaService, PermissionsService permissionsService, ObjectMapper objectMapper, DeleteRelationshipConsumer consumer, KafkaTemplate<String, RelationshipDeletion> kafkaTemplate) {
        super(schemaService, permissionsService, objectMapper);
        this.consumer = consumer;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Before
    public void setUp() throws InterruptedException {
        super.setUp();
    }

    @Given("kafka is available")
    public void kafkaIsAvailable() {
        assertTrue(PermissionsIntegrationTest.kafka.isRunning());
    }

    @When("a delete resource message is sent with resourceId {string} and resourceType {string}")
    public void aUserChecksTheDocument(String resourceId, String resourceType) {
        deleteRelationshipMessage = new RelationshipDeletion(resourceId, resourceType);
        kafkaTemplate.send(P8CKafkaTopic.DELETE_RESOURCE, deleteRelationshipMessage);

    }

    @Then("the message should be consumed by the consumer")
    public void theMessageShouldBeConsumedByTheConsumer() throws InterruptedException {
        boolean messageConsumed = consumer.getLatch().await(10, TimeUnit.SECONDS);

        assertTrue(messageConsumed);
        assertEquals(deleteRelationshipMessage, consumer.getDeleteRelationshipMessage());
    }
}