package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.authzed.api.v1.SchemaServiceOuterClass;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WriteSchemaRequestDto {
    private String schema;
    private String text;

    public SchemaServiceOuterClass.WriteSchemaRequest toWriteSchemaRequest() {
        SchemaServiceOuterClass.WriteSchemaRequest request = SchemaServiceOuterClass.WriteSchemaRequest
                .newBuilder()
                .setSchema(schema)
                .build();
        return request;
    }
}
