package com.synpulse8.pulse8.core.accesscontrolsvc;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/permissions.feature",
        glue = "com.synpulse8.pulse8.core.accesscontrolsvc",
        plugin = {"pretty", "json:target/cucumber-reports/cucumber.json"},
        monochrome = true
)
@CucumberContextConfiguration
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PermissionsIntegrationTest {

    @ClassRule
    public static GenericContainer<?> spicedb = new GenericContainer<>(
            DockerImageName.parse("authzed/spicedb:latest"))
            .withExposedPorts(50051)
            .withCommand("serve --grpc-preshared-key integration_test-key")
            .waitingFor(Wait.forHealthcheck())
            .waitingFor(Wait.forListeningPort())
            .withStartupTimeout(Duration.ofMinutes(2))
            .waitingFor(Wait.forLogMessage(".*grpc server started serving.*\\n", 1));

    @DynamicPropertySource
    static void spicedbProperties(DynamicPropertyRegistry registry) {
        registry.add("SPICEDB_HOST", spicedb::getHost);
        registry.add("SPICEDB_PORT", () -> spicedb.getFirstMappedPort());
        registry.add("SPICEDB_PRESHARED_KEY", () -> "integration_test-key");
    }
}