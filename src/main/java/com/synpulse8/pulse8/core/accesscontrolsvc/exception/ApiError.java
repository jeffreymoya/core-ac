package com.synpulse8.pulse8.core.accesscontrolsvc.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiError {
    @Schema(description = "Timestamp of the error")
    private String timestamp;
    @Schema(description = "HTTP status code of the error")
    private String status;
    @Schema(description = "Error message")
    private String error;
    @Schema(description = "Path of the request that caused the error")
    private String path;
}
