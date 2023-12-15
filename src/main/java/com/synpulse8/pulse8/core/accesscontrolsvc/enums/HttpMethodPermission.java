package com.synpulse8.pulse8.core.accesscontrolsvc.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.P8CException;
import lombok.Getter;

@Getter
public enum HttpMethodPermission {
    GET( "view"),
    PUT( "update"),
    POST( "create"),
    DELETE( "delete");

    private final String permission;

    HttpMethodPermission(String permission) {
        this.permission = permission;
    }

    @JsonCreator
    public static HttpMethodPermission fromString(String value) {
        for (HttpMethodPermission method : HttpMethodPermission.values()) {
            if (method.name().equalsIgnoreCase(value)) {
                return method;
            }
        }
        throw new P8CException("Invalid method value: " + value);
    }
}