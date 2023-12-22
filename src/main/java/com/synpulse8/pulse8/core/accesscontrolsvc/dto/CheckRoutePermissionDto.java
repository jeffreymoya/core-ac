package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.synpulse8.pulse8.core.accesscontrolsvc.enums.HttpMethodPermission;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class CheckRoutePermissionDto {
    @Schema(description = "The route to check permission for", example = "/v1/clients/1234")
    @NotBlank(message = "Route cannot be blank")
    private String route;
    @Schema(description = "The HTTP method to check permission for", example = "GET", defaultValue = "GET")
    private HttpMethodPermission method = HttpMethodPermission.GET;

}
