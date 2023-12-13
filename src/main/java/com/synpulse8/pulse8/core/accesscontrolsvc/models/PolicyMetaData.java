package com.synpulse8.pulse8.core.accesscontrolsvc.models;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;
@Getter
@Builder
@Document
@AllArgsConstructor
public class PolicyMetaData implements PolicyMetaDataBase {
    @Id
    private String id;
    @Indexed(unique = true)
    private String name;
    private String type;
    private String description;
    private Map<String, String> attributes;
}
