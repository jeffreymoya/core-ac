package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.authzed.api.v1.PermissionService;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Getter
@SuperBuilder
public class ReadRelationshipResponseDto extends RelationshipRequestDto {

    public static ReadRelationshipResponseDto from(PermissionService.ReadRelationshipsResponse response) {
        ReadRelationshipResponseDtoBuilder builder = ReadRelationshipResponseDto.builder()
                .objectType(response.getRelationship().getResource().getObjectType())
                .objectId(response.getRelationship().getResource().getObjectId())
                .relation(response.getRelationship().getRelation())
                .subjRefObjType(response.getRelationship().getSubject().getObject().getObjectType())
                .subjRefObjId(response.getRelationship().getSubject().getObject().getObjectId())
                .subjRelation(response.getRelationship().getSubject().getOptionalRelation());

        String caveatName = response.getRelationship().getOptionalCaveat().getCaveatName();
        if (StringUtils.isNotEmpty(caveatName)) builder.caveatName(caveatName);

        return builder.build();

    }

    public static List<ReadRelationshipResponseDto> fromList(Iterator<PermissionService.ReadRelationshipsResponse> iterator) {
        return StreamSupport.stream(((Iterable<PermissionService.ReadRelationshipsResponse>) () -> iterator).spliterator(), false)
                .map(ReadRelationshipResponseDto::from)
                .collect(Collectors.toList());
    }
}