package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.authzed.api.v1.Core;
import com.authzed.api.v1.PermissionService;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Map;


@Getter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "DTO for representing a permission request", subTypes = {
        LookupSubjectsRequestDto.class,
        LookupResourcesRequestDto.class,
        CheckPermissionRequestDto.class
})
@ToString
public class PermissionRequestDto {

    @Schema(description = "Indicates that all data used in the API call must be *at least as fresh* as that found in the ZedToken", example = "GhUKEzE3MDI1NDY2OTI0MjE4NjgxOTk")
    protected String atLeastAsFresh;

    @Schema(description = "// at_exact_snapshot indicates that all data used in the API call must be *at the given* snapshot in time; if the snapshot is no longer available, an error will be returned to the caller.", example = "GhUKEzE3MDI1NDY2OTI0MjE4NjgxOTk")
    protected String atExactSnapshot;

    @Schema(description = "Indicates that all data used in the API call *must* be at the most recent snapshot found", example = "policy")
    protected Boolean fullyConsistent;

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

    @Schema(description = "Context for caveat", example = "{}")
    protected Map<String, Object> context;

    protected PermissionService.Consistency buildConsistency() {
        PermissionService.Consistency.Builder consistencyBuilder = PermissionService.Consistency.newBuilder();

        if(atLeastAsFresh != null){
            consistencyBuilder.setAtLeastAsFresh(Core.ZedToken.newBuilder()
                    .setToken(atLeastAsFresh)
                    .build());
        }

        if(atExactSnapshot != null){
            consistencyBuilder.setAtExactSnapshot(Core.ZedToken.newBuilder()
                    .setToken(atExactSnapshot)
                    .build());
        }

        if(fullyConsistent != null){
            consistencyBuilder.setFullyConsistent(fullyConsistent);
        }

        return consistencyBuilder.setMinimizeLatency(true).build();
    }
}
