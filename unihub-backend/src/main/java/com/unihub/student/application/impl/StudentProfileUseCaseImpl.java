package com.unihub.student.application.impl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.unihub.shared.exception.InvalidOperationException;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.shared.storage.FileStorageService;
import com.unihub.student.api.dto.GraduationCertResponse;
import com.unihub.student.api.dto.StudentProfileResponse;
import com.unihub.student.api.dto.UpdateProfileRequest;
import com.unihub.student.application.StudentProfileMapper;
import com.unihub.student.application.usecase.StudentProfileUseCase;
import com.unihub.student.domain.enums.AcademicStatus;
import com.unihub.student.domain.enums.GraduationCertificateStatus;
import com.unihub.student.domain.event.GraduationCertificateApprovedEvent;
import com.unihub.student.domain.event.GraduationCertificateRejectedEvent;
import com.unihub.student.domain.event.GraduationCertificateSubmittedEvent;
import com.unihub.student.domain.event.StudentProfileUpdatedEvent;
import com.unihub.student.domain.model.GraduationCertificate;
import com.unihub.student.domain.model.StudentProfile;
import com.unihub.student.domain.repository.GraduationCertificateRepository;
import com.unihub.student.domain.repository.SkillRepository;
import com.unihub.student.domain.repository.StudentProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentProfileUseCaseImpl implements StudentProfileUseCase {

    private final StudentProfileRepository studentProfileRepository;
    private final GraduationCertificateRepository gradCertRepo;
    private final SkillRepository skillRepository;
    private final FileStorageService fileStorageService;
    private final ApplicationEventPublisher eventPublisher;
    private final StudentProfileMapper mapper;

    private static final int MAX_CERT_ATTEMPTS = 3;

    @Override
    public StudentProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        var profile = getProfileByUserId(userId);

        if (request.name() != null)
            profile.setName(request.name());

        if (request.bio() != null)
            profile.setBio(request.bio());

        if (request.academicStatus() != null) {
            profile.setAcademicStatus(request.academicStatus());
            if (request.academicStatus() == AcademicStatus.GRADUATE) {
                profile.setLevel(null);
            }
        }

        if (request.level() != null) {
            if (profile.getAcademicStatus() == AcademicStatus.GRADUATE) {
                throw new InvalidOperationException("Graduate students cannot have a level.");
            }
            profile.setLevel(request.level());
        }

        if (request.countryId() != null)
            profile.setCountryId(request.countryId());

        if (request.lookingFor() != null)
            profile.setLookingFor(request.lookingFor());

        if (request.graduationYear() != null)
            profile.setGraduationYear(request.graduationYear());

        var saved = studentProfileRepository.save(profile);
        eventPublisher.publishEvent(new StudentProfileUpdatedEvent(saved.getId(), userId));
        return mapper.toResponse(saved);
    }

    @Override
    public void setUniversity(UUID userId, UUID universityId, UUID majorId) {
        var profile = getProfileByUserId(userId);
        profile.setUniversityOnce(universityId, majorId);
        studentProfileRepository.save(profile);
    }

    @Override
    public void updateSkills(UUID userId, Set<UUID> skillIds) {
        var profile = getProfileByUserId(userId);
        var skills = new HashSet<>(skillRepository.findAllByIdIn(skillIds));
        profile.setSkills(skills);
        studentProfileRepository.save(profile);
    }

    @Override
    public String uploadPhoto(UUID userId, MultipartFile file) {
        var profile = getProfileByUserId(userId);
        String url = fileStorageService.upload(file, "students/photos/" + userId);
        profile.setProfilePhotoUrl(url);
        studentProfileRepository.save(profile);
        return url;
    }

    @Override
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 50, multiplier = 2))
    public GraduationCertResponse uploadGraduationCertificate(UUID userId, MultipartFile file) {
        if (file == null) {
            throw new InvalidOperationException("Please upload a graduation certificate file.");
        }
        var profile = getProfileByUserId(userId);

        if (profile.getUniversityId() == null || profile.getMajorId() == null) {
            throw new InvalidOperationException("You must set your university and major first.");
        }
        if (profile.isVerified()) {
            throw new InvalidOperationException("Your graduation certificate is already approved.");
        }

        int attempts = gradCertRepo.countByStudentId(profile.getId());
        if (attempts >= MAX_CERT_ATTEMPTS) {
            throw new InvalidOperationException("Maximum attempts (3) reached.");
        }

        gradCertRepo.findTopByStudentIdOrderByAttemptNumberDesc(profile.getId())
                .filter(c -> c.getStatus() == GraduationCertificateStatus.PENDING)
                .ifPresent(c -> {
                    throw new InvalidOperationException("You already have a pending certificate.");
                });

        
        var cert = new GraduationCertificate();
        cert.setStudentId(profile.getId());
        cert.setAttemptNumber(attempts + 1);

        GraduationCertificate savedCert = gradCertRepo.save(cert);

        profile.setCertAttempts(attempts + 1);
        studentProfileRepository.save(profile); 

        String fileUrl = fileStorageService.upload(
                file, "students/graduation/" + userId + "/" + savedCert.getId());

        savedCert.setFileUrl(fileUrl);
        gradCertRepo.save(savedCert);

        eventPublisher.publishEvent(
                new GraduationCertificateSubmittedEvent(
                        profile.getId(), profile.getUniversityId(), fileUrl));

        return new GraduationCertResponse(
                savedCert.getId(), savedCert.getStatus(), savedCert.getAttemptNumber(), null);
    }

    @Override
    public void reviewGraduationCertificate(UUID certId, boolean approved, String rejectionReason) {
        var cert = gradCertRepo.findById(certId)
                .orElseThrow(() -> new NotFoundException("Certificate not found"));

        cert.setStatus(approved ? GraduationCertificateStatus.APPROVED : GraduationCertificateStatus.REJECTED);
        cert.setRejectionReason(approved ? null : rejectionReason);
        cert.setReviewedAt(LocalDateTime.now());
        gradCertRepo.save(cert);

        if (approved) {
            studentProfileRepository.findById(cert.getStudentId()).ifPresent(p -> {
                p.setVerified(true);
                studentProfileRepository.save(p);
                eventPublisher.publishEvent(new GraduationCertificateApprovedEvent(p.getId(), p.getUserId()));
            });
        } else {
            eventPublisher.publishEvent(
                    new GraduationCertificateRejectedEvent(cert.getStudentId(), rejectionReason));
        }
    }

    private StudentProfile getProfileByUserId(UUID userId) {
        return studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Student profile not found"));
    }
}