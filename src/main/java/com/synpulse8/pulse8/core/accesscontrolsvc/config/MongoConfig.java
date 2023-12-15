package com.synpulse8.pulse8.core.accesscontrolsvc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.synpulse8.pulse8.core.accesscontrolsvc.repository")
public class MongoConfig {
}
