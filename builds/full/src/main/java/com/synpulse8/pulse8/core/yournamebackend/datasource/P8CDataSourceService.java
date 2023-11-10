package com.synpulse8.pulse8.core.yournamebackend.datasource;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Service
public class P8CDataSourceService implements MultiTenantConnectionProvider, HibernatePropertiesCustomizer {

    private final transient DataSource dataSource;
    private final transient P8CTenantIdentifierResolver tenantIdentifierResolver;

    public P8CDataSourceService(DataSource dataSource, P8CTenantIdentifierResolver tenantIdentifierResolver) {
        this.dataSource = dataSource;
        this.tenantIdentifierResolver = tenantIdentifierResolver;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return getConnection(this.tenantIdentifierResolver.getDefaultTenant());
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    @SuppressWarnings("java:S2095")
    public Connection getConnection(String schema) throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.setSchema(schema);
        return connection;
    }

    @Override
    public void releaseConnection(String schema, Connection connection) throws SQLException {
        connection.setSchema(this.tenantIdentifierResolver.getDefaultTenant());
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, this);
    }

    @Override
    public boolean isUnwrappableAs(Class<?> aClass) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> aClass) {
        return null;
    }



}
