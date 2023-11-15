package com.synpulse8.pulse8.core.accesscontrolsvc.service;

import com.authzed.api.v1.*;
import com.authzed.grpcutil.BearerToken;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SchemaService {
    private static Logger LOGGER = LoggerFactory.getLogger(SchemaService.class);
    private ManagedChannel channel;
    private SchemaServiceGrpc.SchemaServiceBlockingStub schemaService;
    private PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsService;

    @Value("${SPICEDB_HOST}")
    private String spicedbHost;

    @Value("${SPICEDB_PORT}")
    private int spicedbPort;

    @Value("${SPICEDB_PRESHARED_KEY}")
    private String spiceDbPresharedKey;

    @PostConstruct
    public void initialize() {
        LOGGER.info("Starting schema service");
        // Check if the channel is already initialized
        if (channel == null || channel.isTerminated()) {
            channel = ManagedChannelBuilder
                    .forAddress(spicedbHost, spicedbPort)
                    // If SpiceDB requires TLS:
                    // .useTransportSecurity()
                    .usePlaintext()
                    .build();
        }
        BearerToken bearerToken = new BearerToken(spiceDbPresharedKey);
        // Check if the permissionsService is already initialized
        if (schemaService == null) {
            schemaService = SchemaServiceGrpc.newBlockingStub(channel)
                    .withCallCredentials(bearerToken);
        }
        // Check if the permissionsService is already initialized
        if (permissionsService == null) {
            // Initialize the permissionsService only if not already initialized
            permissionsService = PermissionsServiceGrpc.newBlockingStub(channel)
                    .withCallCredentials(bearerToken);
        }
        LOGGER.info("Schema service started");
    }

    public String getSchema() {
        SchemaServiceOuterClass.ReadSchemaRequest request = SchemaServiceOuterClass.ReadSchemaRequest
                .newBuilder()
                .build();

        SchemaServiceOuterClass.ReadSchemaResponse response;
        response = schemaService.readSchema(request);
        String schema = response.getSchemaText();
        return schema;
    }

}
