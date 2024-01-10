package com.synpulse8.pulse8.core.accesscontrolsvc.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class P8CRelationshipError extends P8CError {

    @Schema(description = "SpiceDB relationship list")
    private List relationships;

    public P8CRelationshipError(String error, List relationships) {
        super(error);
        this.relationships = relationships;
    }
}
