package com.synpulse8.pulse8.core.yournamebackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class P8CRestConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
