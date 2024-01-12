package com.synpulse8.pulse8.core.accesscontrolsvc.exception;

import com.synpulse8.pulse8.core.accesscontrolsvc.dto.ReadRelationshipResponseDto;
import lombok.Getter;

import java.util.List;

@Getter
public class P8CRelationshipException extends P8CException {

    private List<ReadRelationshipResponseDto> relationships;

    public P8CRelationshipException(String message, List<ReadRelationshipResponseDto> relationships) {
        super(message);
        this.relationships = relationships;
    }
}
