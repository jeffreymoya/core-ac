package com.synpulse8.pulse8.core.accesscontrolsvc.config;

import com.authzed.api.v1.SchemaServiceGrpc;
import com.authzed.grpcutil.BearerToken;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.authzed.api.v1.PermissionsServiceGrpc;

@Configuration
public class GrpcConfig {

    @Value("${SPICEDB_HOST}")
    private String spicedbHost;

    @Value("${SPICEDB_PORT}")
    private int spicedbPort;

    @Value("${SPICEDB_PRESHARED_KEY}")
    private String spiceDbPresharedKey;

    @Bean
    public PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsService() {
        BearerToken bearerToken = new BearerToken(spiceDbPresharedKey);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(spicedbHost, spicedbPort)
                .usePlaintext()
                .build();
        return PermissionsServiceGrpc.newBlockingStub(channel).withCallCredentials(bearerToken);
    }

    @Bean
    public SchemaServiceGrpc.SchemaServiceBlockingStub schemaService() {
        BearerToken bearerToken = new BearerToken(spiceDbPresharedKey);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(spicedbHost, spicedbPort)
                .usePlaintext()
                .build();
        return SchemaServiceGrpc.newBlockingStub(channel).withCallCredentials(bearerToken);
    }
}