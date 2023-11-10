package com.synpulse8.pulse8.core.yournamebackend.datasource;

import lombok.NonNull;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class P8CTenantIdentifierResolver implements CurrentTenantIdentifierResolver, HibernatePropertiesCustomizer {

    @Value("${yournamebackend.tenant-id}")
    private String defaultTenant;

    @Value("${yournamebackend.tenant-id}")
    private String currentTenant;

    public String getDefaultTenant() {
        return this.defaultTenant;
    }

    public void setCurrentTenant(String tenant) {
        this.currentTenant = tenant;
    }

    @Override
    @NonNull
    public String resolveCurrentTenantIdentifier() {
        return currentTenant;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }

}
