package com.synpulse8.pulse8.core.accesscontrolsvc.enums;

public enum Permission {
    UNSPECIFIED(0),
    HAS_PERMISSION(1),
    CONDITIONAL_PERMISSION(2),
    UNRECOGNIZED(-1);

    private final int value;

    Permission(int i) {
        value = i;
    }

    public int getValue() {
        return value;
    }
}