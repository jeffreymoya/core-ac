package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.authzed.api.v1.PermissionService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@Hidden
public class DeleteRelationshipRequestDto extends RelationshipRequestDto {

    public PermissionService.DeleteRelationshipsRequest toDeleteRelationshipsRequest() {
        PermissionService.DeleteRelationshipsRequest request = PermissionService.DeleteRelationshipsRequest.newBuilder()
                .setRelationshipFilter(buildRelationshipFilter())
                .build();
        return request;
    }

}
