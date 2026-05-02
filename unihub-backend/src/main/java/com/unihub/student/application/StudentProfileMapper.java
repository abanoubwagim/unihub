package com.unihub.student.application;

import org.springframework.stereotype.Component;

import com.unihub.student.api.dto.LinkResponse;
import com.unihub.student.api.dto.StudentProfileResponse;
import com.unihub.student.domain.model.StudentProfile;

@Component
public class StudentProfileMapper {

    public StudentProfileResponse toResponse(StudentProfile profile) {
        return new StudentProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getName(),
                profile.getBio(),
                profile.getProfilePhotoUrl(),
                profile.getAcademicStatus(),
                profile.getLevel(),
                profile.getUniversityId(),
                profile.getMajorId(),
                profile.getCountryId(),
                profile.getLookingFor(),
                profile.getGraduationYear(),
                profile.isVerified(),
                profile.getSkills().stream().map(s -> s.getName()).toList(),
                profile.getLinks().stream()
                        .map(l -> new LinkResponse(l.getLinkType(), l.getLabel(), l.getUrl()))
                        .toList()
        );
    }
}