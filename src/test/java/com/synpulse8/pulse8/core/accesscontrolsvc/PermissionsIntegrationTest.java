package com.synpulse8.pulse8.core.accesscontrolsvc;

import io.cucumber.java.BeforeAll;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@CucumberContextConfiguration
@ActiveProfiles("test")
@Import(KafkaTestContainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = PermissionsIntegrationTest.DataSourceInitializer.class)
public class PermissionsIntegrationTest {

    public static String bootstrapServers;

    @Container
    public static GenericContainer<?> spicedb = new GenericContainer<>(
            DockerImageName.parse("authzed/spicedb:latest"))
            .withExposedPorts(50051)
            .withCommand("serve --grpc-preshared-key integration_test-key")
            .waitingFor(Wait.forLogMessage(".*grpc server started serving.*\\n", 1));

    @Container
    public static MongoDBContainer mongodb = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    public static KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"));


    public static class DataSourceInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            spicedb.start();
            mongodb.start();
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "SPICEDB_HOST=" + spicedb.getHost(),
                    "SPICEDB_PORT=" + spicedb.getFirstMappedPort(),
                    "SPICEDB_PRESHARED_KEY=integration_test-key",
                    "spring.data.mongodb.uri=" + mongodb.getConnectionString() + "/p8c-core-access-control"
            );
        }
    }

    @BeforeAll
    public static void startKafkaContainer() {
        kafka.start();
        bootstrapServers = kafka.getBootstrapServers();
        System.setProperty("spring.kafka.bootstrap-servers", bootstrapServers);
    }

}