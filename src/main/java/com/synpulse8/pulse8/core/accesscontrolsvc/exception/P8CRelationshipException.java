package com.synpulse8.pulse8.core.accesscontrolsvc.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class P8CRelationshipException extends P8CException {

    private List<Object> relationships;

    public P8CRelationshipException(String message, List<Object> relationships) {
        super(message);
        this.relationships = relationships;
    }
}
