package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.authzed.api.v1.PermissionService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class DeleteRelationshipRequestDto extends RelationshipRequestDto {

    public PermissionService.DeleteRelationshipsRequest toDeleteRelationshipsRequest() {
        PermissionService.DeleteRelationshipsRequest request = PermissionService.DeleteRelationshipsRequest.newBuilder()
                .setRelationshipFilter(buildRelationshipFilter())
                .build();
        return request;
    }

}
