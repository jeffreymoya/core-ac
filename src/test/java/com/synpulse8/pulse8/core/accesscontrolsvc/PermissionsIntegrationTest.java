package com.synpulse8.pulse8.core.accesscontrolsvc;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.runner.RunWith;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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
@EnableAutoConfiguration(exclude= {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class})
public class PermissionsIntegrationTest {

}