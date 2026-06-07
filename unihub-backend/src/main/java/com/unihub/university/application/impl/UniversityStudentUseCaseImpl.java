package com.unihub.university.application.impl;

import com.unihub.shared.api.dto.PageResponse;
import com.unihub.shared.api.dto.external.StudentPublicInfo;
import com.unihub.shared.api.external.StudentPublicApi;
import com.unihub.shared.exception.InvalidOperationException;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.university.api.dto.res.UniversityStudentSummaryResponse;
import com.unihub.university.application.usecase.UniversityStudentUseCase;
import com.unihub.university.domain.model.UniversityProfile;
import com.unihub.university.domain.repository.UniversityProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UniversityStudentUseCaseImpl implements UniversityStudentUseCase {

    private final UniversityProfileRepository universityProfileRepository;
    private final StudentPublicApi studentPublicApi;

    @Override
    public PageResponse<UniversityStudentSummaryResponse> getMyStudents(UUID userId, Pageable pageable) {
        UniversityProfile profile = getProfileByUserId(userId);
        Page<StudentPublicInfo> page = studentPublicApi.getStudentsByUniversityId(profile.getId(), pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Override
    public UniversityStudentSummaryResponse getStudent(UUID userId, UUID studentProfileId) {
        UniversityProfile profile = getProfileByUserId(userId);

        StudentPublicInfo student = studentPublicApi.getStudentsByUniversityId(profile.getId(), Pageable.unpaged())
                .stream()
                .filter(s -> s.profileId().equals(studentProfileId))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Unauthorized student access — universityUserId={}, studentProfileId={}", userId, studentProfileId);
                    return new InvalidOperationException("This student does not belong to your university.");
                });

        return toResponse(student);
    }

    private UniversityProfile getProfileByUserId(UUID userId) {
        return universityProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("University profile not found"));
    }

    private UniversityStudentSummaryResponse toResponse(StudentPublicInfo info) {
        return new UniversityStudentSummaryResponse(
                info.profileId(),
                info.userId(),
                info.name(),
                info.profilePhotoUrl(),
                info.majorId(),
                info.level());
    }
}