package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.authzed.api.v1.SchemaServiceOuterClass;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WriteSchemaRequestDto {
    private String schema;

    public SchemaServiceOuterClass.WriteSchemaRequest toWriteSchemaRequest() {
        SchemaServiceOuterClass.WriteSchemaRequest request = SchemaServiceOuterClass.WriteSchemaRequest
                .newBuilder()
                .setSchema(schema)
                .build();
        return request;
    }
}
