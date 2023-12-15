package com.synpulse8.pulse8.core.accesscontrolsvc.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.P8CException;

public enum Access {
    ALLOW,
    DENY,
    LOG;

    @JsonCreator
    public static Access fromString(String value) {
        for (Access access : Access.values()) {
            if (access.name().equalsIgnoreCase(value)) {
                return access;
            }
        }
        throw new P8CException("Invalid access value: " + value);
    }
}
