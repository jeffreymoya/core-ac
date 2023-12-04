package com.synpulse8.pulse8.core.accesscontrolsvc.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiError {
    @Schema(description = "Timestamp of the error", example = "2021-07-01T12:00:00.000Z")
    private String timestamp;
    @Schema(description = "HTTP status code of the error", example = "403")
    private String status;
    @Schema(description = "Error message", example = "Forbidden")
    private String error;
    @Schema(description = "Path of the request that caused the error", example = "/api/v1/action")
    private String path;
}
