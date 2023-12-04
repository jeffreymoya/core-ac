package com.synpulse8.pulse8.core.accesscontrolsvc.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
@Getter
public class P8CError {
    public P8CError(String error) {
        this.error = error;
    }

    @Schema(description = "SpiceDB error message")
    @NotNull(message = "SpiceDB error message cannot be null")
    private String error;
}
