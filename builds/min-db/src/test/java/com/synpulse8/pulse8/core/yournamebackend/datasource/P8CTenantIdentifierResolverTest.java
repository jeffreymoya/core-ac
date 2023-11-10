package com.synpulse8.pulse8.core.yournamebackend.datasource;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Log4j2
@ExtendWith(MockitoExtension.class)
class P8CTenantIdentifierResolverTest {

    @InjectMocks
    private P8CTenantIdentifierResolver tenantIdentifierResolver;

    @Test
    void setCurrentTenant() {
        this.tenantIdentifierResolver.setCurrentTenant("test");
        assertThat(this.tenantIdentifierResolver.resolveCurrentTenantIdentifier()).isEqualTo("test");
    }

    @Test
    void getDefaultTenant() {
        assertNull(this.tenantIdentifierResolver.getDefaultTenant());
    }

    @Test
    void validateExistingCurrentSessions() {
        assertFalse(this.tenantIdentifierResolver.validateExistingCurrentSessions());
    }

    @Test
    void customize() {
        Map<String, Object> map = new HashMap<>();
        this.tenantIdentifierResolver.customize(map);
        assertThat(map).isNotEmpty();
    }

}
