package com.synpulse8.pulse8.core.accesscontrolsvc.models;

import com.synpulse8.pulse8.core.accesscontrolsvc.enums.Access;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;
@Getter
@Setter
@Builder
@Document
@AllArgsConstructor
public class PolicyMetaData implements PolicyMetaDataBase {
    @Id
    private String id;
    @Indexed(unique = true)
    private String name;
    private String description;
    private Map<String, Object> attributes;
    private Access access;
}
