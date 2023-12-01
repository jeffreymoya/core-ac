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
    private SubjectDto subject;
    private List<SubjectDto> excludedSubjects;

    public static LookupSubjectsResponseDto from(PermissionService.LookupSubjectsResponse response) {

        return LookupSubjectsResponseDto.builder()
                .subject(SubjectDto.from(response.getSubject()))
                .excludedSubjects(SubjectDto.fromList(response.getExcludedSubjectsList()))
                .build();
    }

    public static List<LookupSubjectsResponseDto> fromList(Iterator<PermissionService.LookupSubjectsResponse> iterator) {
        return StreamSupport.stream(((Iterable<PermissionService.LookupSubjectsResponse>) () -> iterator).spliterator(), false)
                .map(LookupSubjectsResponseDto::from)
                .collect(Collectors.toList());
    }
}