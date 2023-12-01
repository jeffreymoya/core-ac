package com.synpulse8.pulse8.core.accesscontrolsvc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class OpenApiConfig {

    @Value("${p8c.security.principal-header}")
    private String principalHeader;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().schemaRequirement(principalHeader,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(principalHeader))
        .addSecurityItem(new SecurityRequirement().addList(principalHeader, Collections.emptyList()));
    }
}