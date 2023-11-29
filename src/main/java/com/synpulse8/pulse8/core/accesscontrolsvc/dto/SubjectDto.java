package com.synpulse8.pulse8.core.accesscontrolsvc.dto;

import com.authzed.api.v1.PermissionService;
import lombok.Builder;
import lombok.Getter;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Getter
@Builder
public class SubjectDto {
    private int permission;
    private String subjectObjectId;

    public static SubjectDto from(PermissionService.ResolvedSubject subject) {
        return SubjectDto.builder()
                .permission(subject.getPermissionshipValue())
                .subjectObjectId(subject.getSubjectObjectId())
                .build();
    }

    public static List<SubjectDto> fromList(List<PermissionService.ResolvedSubject> subjectList) {
        return subjectList.stream()
                .map(SubjectDto::from)
                .collect(Collectors.toList());
    }
}
