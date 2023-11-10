package com.synpulse8.pulse8.core.yournamebackend.config;

import com.synpulse8.pulse8.core.yournamebackend.datasource.P8CTenantIdentifierResolver;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@ConditionalOnProperty(value = "yournamebackend.hikari.enabled", havingValue = "true")
public class P8CHikariDataSourceConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    private final P8CTenantIdentifierResolver tenantIdentifierResolver;

    public P8CHikariDataSourceConfig(P8CTenantIdentifierResolver tenantIdentifierResolver) {
        this.tenantIdentifierResolver = tenantIdentifierResolver;
    }

    @Bean
    public HikariDataSource hikariDataSource() {

        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(this.jdbcUrl);
        hikariConfig.setSchema(this.tenantIdentifierResolver.getDefaultTenant());
        hikariConfig.setDriverClassName(this.driverClassName);
        hikariConfig.setUsername(this.username);
        hikariConfig.setPassword(this.password);
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setIdleTimeout(600000);
        hikariConfig.setMaxLifetime(1800000);
        hikariConfig.setLeakDetectionThreshold(120000);
        hikariConfig.setMaximumPoolSize(20);
        hikariConfig.setMinimumIdle(5);

        Properties dataSourceProperties = new Properties();

        dataSourceProperties.setProperty("cachePrepStmts", "true");
        dataSourceProperties.setProperty("rewriteBatchedStatements", "true");
        dataSourceProperties.setProperty("currentSchema", this.tenantIdentifierResolver.getDefaultTenant());

        hikariConfig.setDataSourceProperties(dataSourceProperties);

        return new HikariDataSource(hikariConfig);

    }

}
