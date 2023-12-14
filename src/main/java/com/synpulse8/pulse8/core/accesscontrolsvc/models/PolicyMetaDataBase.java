package com.synpulse8.pulse8.core.accesscontrolsvc.models;

import java.util.Map;


public interface PolicyMetaDataBase {
    String getId();
    String getName();
    String getDescription();
    Map<String, String> getAttributes();
}