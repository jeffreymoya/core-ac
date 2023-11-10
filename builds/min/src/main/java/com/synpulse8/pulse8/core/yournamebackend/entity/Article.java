package com.synpulse8.pulse8.core.yournamebackend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Article {

    @JsonProperty
    private Integer id;

    @JsonProperty
    private String caption;
}
