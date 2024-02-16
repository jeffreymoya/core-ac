package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.synpulse8.pulse8.core.accesscontrolsvc.enums.HttpMethodPermission;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Getter
@SuperBuilder
@NoArgsConstructor
public class CheckRoutePermissionDto {
    @Schema(description = "The route to check permission for", example = "/v1/clients/1234")
    @NotBlank(message = "Route cannot be blank")
    private String route;
    @Schema(description = "The HTTP method to check permission for", example = "GET", defaultValue = "GET")
    private HttpMethodPermission method = HttpMethodPermission.GET;
    @JsonProperty("uri_template")
    private String uriTemplate;
    private String objectId;
    private String objectType;
    @Schema(description = "Context for caveat", example = "{}")
    protected Map<String, Object> context;

}
