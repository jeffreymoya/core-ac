package com.synpulse8.pulse8.core.accesscontrolsvc.models;

import com.synpulse8.pulse8.core.accesscontrolsvc.enums.Access;

import java.util.Map;


public interface PolicyMetaDataBase {
    String getId();
    String getName();
    String getDescription();
    Map<String, Object> getAttributes();

    Access getAccess();
}