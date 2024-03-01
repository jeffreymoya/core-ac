package com.synpulse8.pulse8.core.accesscontrolsvc.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeleteRelationshipMessage {
    private String resourceId;
    private String resourceType;

}
