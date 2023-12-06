package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.google.protobuf.Value;

import java.util.Map;
import java.util.stream.Collectors;

public class ContextMapper {

    public static Map<String, Value> convertMap(Map<String, Object> originalMap) {
        return originalMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> toValue(entry.getValue())
                ));
    }

    private static Value toValue(Object value) {
        try {
            return Value.newBuilder()
                    .setStringValue(value.toString())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error converting object to Value", e);
        }
    }
}
