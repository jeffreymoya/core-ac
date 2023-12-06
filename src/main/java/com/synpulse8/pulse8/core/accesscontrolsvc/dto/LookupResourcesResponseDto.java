package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.authzed.api.v1.PermissionService;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Getter
@Builder
public class LookupResourcesResponseDto {
    private int permission;
    private String resourceObjectId;

    public static LookupResourcesResponseDto from(PermissionService.LookupResourcesResponse response) {
        return LookupResourcesResponseDto.builder()
                .permission(response.getPermissionshipValue())
                .resourceObjectId(response.getResourceObjectId())
                .build();
    }

    public static List<LookupResourcesResponseDto> fromList(Iterator<PermissionService.LookupResourcesResponse> iterator) {
        return StreamSupport.stream(((Iterable<PermissionService.LookupResourcesResponse>) () -> iterator).spliterator(), false)
                .map(LookupResourcesResponseDto::from)
                .collect(Collectors.toList());
    }
}