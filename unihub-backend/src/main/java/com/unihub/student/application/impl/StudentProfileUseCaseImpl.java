package com.unihub.student.application.impl;

import com.unihub.shared.exception.InvalidOperationException;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.shared.storage.FileStorageService;
import com.unihub.student.api.dto.req.UpdateProfileRequest;
import com.unihub.student.api.dto.res.GraduationCertResponse;
import com.unihub.student.api.dto.res.StudentProfileResponse;
import com.unihub.student.application.StudentProfileMapper;
import com.unihub.student.application.usecase.StudentProfileUseCase;
import com.unihub.student.domain.enums.AcademicStatus;
import com.unihub.student.domain.enums.GraduationCertificateStatus;
import com.unihub.student.domain.event.*;
import com.unihub.student.domain.model.GraduationCertificate;
import com.unihub.student.domain.model.Skill;
import com.unihub.student.domain.model.StudentProfile;
import com.unihub.student.domain.repository.GraduationCertificateRepository;
import com.unihub.student.domain.repository.SkillRepository;
import com.unihub.student.domain.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StudentProfileUseCaseImpl implements StudentProfileUseCase {

    private static final int MAX_CERT_ATTEMPTS = 3;

    private final StudentProfileRepository studentProfileRepository;
    private final GraduationCertificateRepository gradCertRepo;
    private final SkillRepository skillRepository;
    private final FileStorageService fileStorageService;
    private final ApplicationEventPublisher eventPublisher;
    private final StudentProfileMapper mapper;

    @Override
    public StudentProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        log.debug("Updating profile for userId={}", userId);

        StudentProfile profile = getProfileByUserId(userId);

        if (request.name() != null)
            profile.setName(request.name());

        if (request.bio() != null)
            profile.setBio(request.bio());

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

        StudentProfile saved = studentProfileRepository.save(profile);
        log.info("Profile updated successfully for userId={}, profileId={}", userId, saved.getId());
        eventPublisher.publishEvent(new StudentProfileUpdatedEvent(saved.getId(), userId));
        return mapper.toResponse(saved);
    }


    @Override
    public void updateSkills(UUID userId, Set<UUID> skillIds) {
        log.debug("Updating skills for userId={}, skillIds={}", userId, skillIds);

        StudentProfile profile = getProfileByUserId(userId);
        Set<Skill> skills = new HashSet<>(skillRepository.findAllByIdIn(skillIds));
        profile.getSkills().addAll(skills);
        studentProfileRepository.save(profile);
        log.info("Skills updated for userId={}, count={}", userId, skills.size());
    }

    @Override
    public String uploadPhoto(UUID userId, MultipartFile file) {
        log.debug("Uploading photo for userId={}", userId);
        StudentProfile profile = getProfileByUserId(userId);

        String oldUrl = profile.getProfilePhotoUrl();
        if (oldUrl != null && !oldUrl.isBlank()) {
            log.debug("Deleting old photo for userId={}, url={}", userId, oldUrl);
            fileStorageService.delete(oldUrl);
        }

        String url = fileStorageService.upload(file, "students/photos/" + userId);
        profile.setProfilePhotoUrl(url);
        studentProfileRepository.save(profile);
        log.info("Student photo uploaded for userId={}, url={}", userId, url);
        return url;
    }

    @Override
    @Transactional
    public GraduationCertResponse uploadGraduationCertificate(UUID userId, MultipartFile file) {

        log.debug("Uploading graduation certificate for userId={}", userId);

        StudentProfile profile = getProfileByUserId(userId);

        if (file == null) {
            log.warn("Null file submitted for graduation cert — userId={}", userId);
            throw new InvalidOperationException("Please upload a graduation certificate file.");
        }

        if (profile.getUniversityId() == null || profile.getMajorId() == null) {
            log.warn("University or major not set for userId={}", userId);
            throw new InvalidOperationException("You must set your university and major first.");
        }

        if (profile.isCertVerified()) {
            log.warn("Certificate already approved for userId={}", userId);
            throw new InvalidOperationException("Your graduation certificate is already approved.");
        }

        if (profile.isCertificateLocked()) {
            log.warn("Certificate locked for userId={}", userId);
            throw new InvalidOperationException("You reached maximum attempts. Please contact support.");
        }

        int attempts = gradCertRepo.countByStudentIdAndStatus(profile.getId(), GraduationCertificateStatus.REJECTED);
        log.debug("Rejection attempts so far for profileId={}: {}", profile.getId(), attempts);
        if (attempts >= MAX_CERT_ATTEMPTS) {  // 3
            log.warn("Max cert attempts reached for userId={}, locking profile", userId);
            profile.setCertificateLocked(true);
            studentProfileRepository.save(profile);
            throw new InvalidOperationException("You reached maximum attempts. Please contact support.");
        }

        gradCertRepo.findTopByStudentIdOrderByAttemptNumberDesc(profile.getId())
                .filter(c -> c.getStatus() == GraduationCertificateStatus.PENDING)
                .ifPresent(c -> {
                    log.warn("Pending certificate already exists for userId={}, certId={}", userId, c.getId());
                    throw new InvalidOperationException("You already have a pending certificate.");
                });


        GraduationCertificate cert = new GraduationCertificate();
        cert.setStudentId(profile.getId());
        cert.setUniversityId(profile.getUniversityId());

        GraduationCertificate savedCert = gradCertRepo.save(cert);
        savedCert.setAttemptNumber(attempts + 1);

        String fileUrl = fileStorageService.upload(
                file, "students/graduation/" + userId + "/" + savedCert.getId());

        savedCert.setFileUrl(fileUrl);
        gradCertRepo.save(savedCert);

        log.info("Graduation cert uploaded — userId={}, certId={}, attempt={}, url={}",
                userId, savedCert.getId(), savedCert.getAttemptNumber(), fileUrl);

        eventPublisher.publishEvent(
                new GraduationCertificateSubmittedEvent(
                        profile.getId(), profile.getUniversityId(), fileUrl));

        return new GraduationCertResponse(
                savedCert.getId(), savedCert.getStatus(), savedCert.getAttemptNumber(), null);
    }

    @Override
    public void reviewGraduationCertificate(UUID certId, UUID reviewerUniversityId, boolean approved, String rejectionReason) {
        log.debug("Reviewing cert — certId={}, reviewerUniversityId={}, approved={}", certId, reviewerUniversityId, approved);

        GraduationCertificate cert = gradCertRepo.findById(certId)
                .orElseThrow(() -> new NotFoundException("Certificate not found"));

        if (!cert.getUniversityId().equals(reviewerUniversityId)) {
            log.warn("Unauthorized review attempt — certId={}, reviewerUniversityId={}", certId, reviewerUniversityId);
            throw new InvalidOperationException("You are not authorized to review this certificate.");
        }

        if (!approved && (rejectionReason == null || rejectionReason.isBlank())) {
            log.warn("Rejection reason missing — certId={}", certId);
            throw new InvalidOperationException("Rejection reason is required.");
        }

        if (cert.getStatus() != GraduationCertificateStatus.PENDING) {
            log.warn("Certificate already reviewed — certId={}", certId);
            throw new InvalidOperationException("This certificate has already been reviewed.");
        }

        cert.setStatus(approved ? GraduationCertificateStatus.APPROVED : GraduationCertificateStatus.REJECTED);
        cert.setRejectionReason(approved ? null : rejectionReason);
        cert.setReviewedAt(LocalDateTime.now());
        gradCertRepo.save(cert);

        StudentProfile profile = studentProfileRepository.findById(cert.getStudentId())
                .orElseThrow(() -> new NotFoundException("Student profile not found"));

        if (approved) {
            log.info("Cert approved — certId={}, studentId={}", certId, profile.getId());
            profile.setCertVerified(true);
            profile.setAcademicStatus(AcademicStatus.GRADUATE);
            profile.setLevel(null);
            studentProfileRepository.save(profile);
            eventPublisher.publishEvent(
                    new GraduationCertificateApprovedEvent(
                            profile.getId(), profile.getUserId(), profile.getUniversityId()));
        } else {
            int totalRejections = gradCertRepo.countByStudentIdAndStatus(profile.getId(), GraduationCertificateStatus.REJECTED);
            log.info("Cert rejected — certId={}, studentId={}, totalRejections={}, reason={}",
                    certId, profile.getId(), totalRejections, rejectionReason);
            if (totalRejections >= MAX_CERT_ATTEMPTS) {
                log.warn("Max rejections reached — locking profileId={}", profile.getId());
                profile.setCertificateLocked(true);
                studentProfileRepository.save(profile);
            }
            eventPublisher.publishEvent(
                    new GraduationCertificateRejectedEvent(cert.getStudentId(), cert.getUniversityId(), rejectionReason));
        }
    }

    @Override
    public void setUniversityOnce(UUID userId, UUID universityId, UUID majorId) {
        log.debug("Setting university for userId={}, universityId={}, majorId={}", userId, universityId, majorId);

        StudentProfile profile = getProfileByUserId(userId);
        if (profile.getUniversityId() != null || profile.getMajorId() != null) {
            log.warn("University already set for userId={}", userId);
            throw new InvalidOperationException("University and major can only be set once.");
        }
        profile.setUniversityId(universityId);
        profile.setMajorId(majorId);
        studentProfileRepository.save(profile);
        eventPublisher.publishEvent(new StudentUniversitySetEvent(profile.getId(), universityId));
        log.info("University set for userId={}, universityId={}, majorId={}", userId, universityId, majorId);
    }

    private StudentProfile getProfileByUserId(UUID userId) {
        return studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Student profile not found"));
    }
}