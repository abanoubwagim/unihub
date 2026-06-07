package com.unihub.university.application.impl;

import com.unihub.shared.api.dto.PageResponse;
import com.unihub.university.api.dto.res.MajorResponse;
import com.unihub.university.api.dto.res.UniversityPublicResponse;
import com.unihub.university.application.usecase.UniversityPublicUseCase;
import com.unihub.university.domain.model.UniversityProfile;
import com.unihub.university.domain.repository.UniversityProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UniversityPublicUseCaseImpl implements UniversityPublicUseCase {

    private final UniversityProfileRepository universityProfileRepository;

    @Override
    @Cacheable(value = "universities:public", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public PageResponse<UniversityPublicResponse> getAll(Pageable pageable) {
        return PageResponse.from(
                universityProfileRepository.findAll(pageable)
                        .map(this::toResponse));
    }

    private UniversityPublicResponse toResponse(UniversityProfile p) {
        return new UniversityPublicResponse(
                p.getId(),
                p.getName(),
                p.getProfilePhotoUrl(),
                p.getWebsiteUrl(),
                p.getCountryId(),
                p.getStudentCount(),
                p.getMajors().stream()
                        .map(m -> new MajorResponse(m.getId(), m.getName()))
                        .toList()
        );
    }
}