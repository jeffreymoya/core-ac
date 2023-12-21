package com.synpulse8.pulse8.core.accesscontrolsvc.models;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ZedResource {

    private String objectType;
    private String objectId;
}
