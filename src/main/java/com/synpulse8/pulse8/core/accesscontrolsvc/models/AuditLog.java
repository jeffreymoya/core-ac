package com.synpulse8.pulse8.core.accesscontrolsvc.models;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class AuditLog {

    private String timestamp;
    private String userId;
    private String methodName;
    private String topic;
    private String path;
    private String queryString;
    private String details;
    private String response;
    private String stepName;

}
