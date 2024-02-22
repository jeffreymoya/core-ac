package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.synpulse8.pulse8.core.accesscontrolsvc.exception.P8CException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ContextMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Map<String, Value> convertMap(Map<String, Object> originalMap) {
        return originalMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> toValue(entry.getValue())
                ));
    }

    private static ListValue.Builder convertList(List<?> list) {
        ListValue.Builder listValueBuilder = ListValue.newBuilder();
        list.forEach(item -> listValueBuilder.addValues(toValue(item)));
        return listValueBuilder;
    }

    private static Value toValue(Object value) {
        Value.Builder builder = Value.newBuilder();
        if (value instanceof String) return builder.setStringValue(value.toString()).build();
        else if (value instanceof Integer) return builder.setNumberValue((Integer) value).build();
        else if (value instanceof Double) return builder.setNumberValue((Double) value).build();
        else if (value instanceof Boolean) return builder.setBoolValue((Boolean) value).build();
        else if (value instanceof List) return builder.setListValue(convertList((List<?>) value).build()).build();
        else if (value instanceof Map) {
            Map<String, Object> valueMap = objectMapper.convertValue(value, new TypeReference<>() {});
            Struct.Builder structBuilder = Struct.newBuilder().putAllFields(ContextMapper.convertMap(valueMap));
            return builder.setStructValue(structBuilder.build()).build();
        }
        else if (value == null) return builder.setNullValueValue(0).build();
        else {
            throw new P8CException("Unsupported type for value: " + value);
        }
    }
}
