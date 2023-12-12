package com.synpulse8.pulse8.core.accesscontrolsvc.enums;

public enum HttpMethodPermission {
    VIEW("GET", "view"),
    UPDATE("PUT", "update"),
    CREATE("POST", "create"),
    DELETE("DELETE", "delete");

    private final String value;
    private final String permission;

    HttpMethodPermission(String value, String permission) {
        this.value = value;
        this.permission = permission;
    }

    public String getValue() {
        return value;
    }

    public String getPermission() {
        return permission;
    }

    public static HttpMethodPermission fromValue(String value) {
        for (HttpMethodPermission permission : HttpMethodPermission.values()) {
            if (permission.getValue().equals(value)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("Invalid HttpMethodPermission value: " + value);
    }
}