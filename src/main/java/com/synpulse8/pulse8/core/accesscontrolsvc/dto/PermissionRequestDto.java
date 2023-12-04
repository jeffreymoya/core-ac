package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Getter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "DTO for representing a permission request", subTypes = {
        LookupSubjectsRequestDto.class,
        LookupResourcesRequestDto.class,
        CheckPermissionRequestDto.class
})
public class PermissionRequestDto {

    @Schema(description = "The type of resource that is requested", example = "policy")
    protected String objectType;

    @Schema(description = "The ID of the resource that is requested", example = "doc001")
    protected String objectId;

    @Schema(description = "Type of the subject reference", example ="user")
    protected String subjRefObjType;

    @Schema(description = "ID of the subject reference", example = "john01")
    protected String subjRefObjId;

    @Schema(description = "Subject relation", example = "employee")
    protected String subjRelation;

    @Schema(description = "Requested permission", example = "create_policy")
    @NotBlank(message = "Permission cannot be null")
    @Size(min = 1, message = "Permission must not be empty")
    protected String permission;
}
