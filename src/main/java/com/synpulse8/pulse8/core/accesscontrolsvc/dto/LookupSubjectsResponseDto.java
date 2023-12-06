package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.authzed.api.v1.PermissionService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Getter
@Builder
@Schema(description = "DTO for list of subjects")
public class LookupSubjectsResponseDto {
    @Schema(description = "Permission value for the subject: 0 (unspecified), 1 (has permission), 2 (conditional permission), -1 (unrecognized)", allowableValues = {"0", "1", "2", "-1"})
    private int permission;
    private String subjectObjectId;

    public static LookupSubjectsResponseDto from(PermissionService.LookupSubjectsResponse response) {
        return LookupSubjectsResponseDto.builder()
                .subjectObjectId(response.getSubject().getSubjectObjectId())
                .permission(response.getSubject().getPermissionshipValue())
                .build();
    }

    public static List<LookupSubjectsResponseDto> fromList(Iterator<PermissionService.LookupSubjectsResponse> iterator) {
        return StreamSupport.stream(((Iterable<PermissionService.LookupSubjectsResponse>) () -> iterator).spliterator(), false)
                .map(LookupSubjectsResponseDto::from)
                .collect(Collectors.toList());
    }
}