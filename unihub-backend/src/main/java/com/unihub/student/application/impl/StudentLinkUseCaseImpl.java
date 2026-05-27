package com.unihub.student.application.impl;

import com.unihub.shared.exception.NotFoundException;
import com.unihub.student.api.dto.req.LinkRequest;
import com.unihub.student.api.dto.res.LinkResponse;
import com.unihub.student.application.usecase.StudentLinkUseCase;
import com.unihub.student.domain.model.StudentLink;
import com.unihub.student.domain.model.StudentProfile;
import com.unihub.student.domain.repository.StudentLinkRepository;
import com.unihub.student.domain.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StudentLinkUseCaseImpl implements StudentLinkUseCase {

    private final StudentProfileRepository profileRepository;
    private final StudentLinkRepository linkRepository;

    @Override
    public LinkResponse add(UUID userId, LinkRequest request) {
        log.debug("Adding link for userId={}, type={}", userId, request.linkType());

        StudentProfile profile = getProfileByUserId(userId);

        StudentLink link = StudentLink.builder()
                .student(profile)
                .linkType(request.linkType())
                .label(request.label())
                .url(request.url())
                .build();

        LinkResponse response = toResponse(linkRepository.save(link));
        log.info("Link added — userId={}, linkId={}, type={}", userId, response.type(), response.url());
        return response;
    }

    @Override
    public LinkResponse update(UUID userId, UUID linkId, LinkRequest request) {
        log.debug("Updating link — userId={}, linkId={}", userId, linkId);

        StudentLink link = getOwnedLink(userId, linkId);

        link.setLinkType(request.linkType());
        link.setLabel(request.label());
        link.setUrl(request.url());

        LinkResponse response = toResponse(linkRepository.save(link));
        log.info("Link updated — userId={}, linkId={}", userId, linkId);
        return response;
    }

    @Override
    public void delete(UUID userId, UUID linkId) {
        log.debug("Deleting link — userId={}, linkId={}", userId, linkId);

        linkRepository.delete(getOwnedLink(userId, linkId));
        log.info("Link deleted — userId={}, linkId={}", userId, linkId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LinkResponse> getAll(UUID userId) {
        return linkRepository.findAllByStudent_UserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LinkResponse getOne(UUID userId, UUID linkId) {
        return toResponse(getOwnedLink(userId, linkId));
    }

    private StudentLink getOwnedLink(UUID userId, UUID linkId) {
        return linkRepository.findByIdAndStudent_UserId(linkId, userId)
                .orElseThrow(() -> {
                    log.warn("Unauthorized or missing link access — userId={}, linkId={}", userId, linkId);
                    return new NotFoundException("Link not found");
                });
    }

    private StudentProfile getProfileByUserId(UUID userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Student profile not found"));
    }

    private LinkResponse toResponse(StudentLink link) {
        return new LinkResponse(
                link.getId(),
                link.getLinkType(),
                link.getLabel(),
                link.getUrl()
        );
    }
}