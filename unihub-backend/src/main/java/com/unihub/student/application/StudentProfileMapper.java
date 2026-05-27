package com.unihub.student.application;

import com.unihub.student.api.dto.res.LinkResponse;
import com.unihub.student.api.dto.res.StudentProfileResponse;
import com.unihub.student.domain.model.Skill;
import com.unihub.student.domain.model.StudentProfile;
import org.springframework.stereotype.Component;

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
                profile.getSkills().stream().map(Skill::getName).toList(),
                profile.getLinks().stream()
                        .map(l -> new LinkResponse(l.getId(), l.getLinkType(), l.getLabel(), l.getUrl()))
                        .toList()
        );
    }
}