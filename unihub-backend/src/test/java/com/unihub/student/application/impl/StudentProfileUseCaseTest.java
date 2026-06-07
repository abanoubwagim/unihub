package com.unihub.student.application.impl;

import com.unihub.shared.api.external.UniversityPartnershipApi;
import com.unihub.shared.exception.InvalidOperationException;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.shared.storage.FileStorageService;
import com.unihub.student.api.dto.req.UpdateProfileRequest;
import com.unihub.student.api.dto.res.GraduationCertResponse;
import com.unihub.student.api.dto.res.StudentProfileResponse;
import com.unihub.student.application.StudentProfileMapper;
import com.unihub.student.domain.enums.AcademicStatus;
import com.unihub.student.domain.enums.GraduationCertificateStatus;
import com.unihub.student.domain.enums.StudentLevel;
import com.unihub.student.domain.event.GraduationCertificateSubmittedEvent;
import com.unihub.student.domain.event.StudentProfileUpdatedEvent;
import com.unihub.student.domain.event.StudentUniversitySetEvent;
import com.unihub.student.domain.model.GraduationCertificate;
import com.unihub.student.domain.model.Skill;
import com.unihub.student.domain.model.StudentProfile;
import com.unihub.student.domain.repository.GraduationCertificateRepository;
import com.unihub.student.domain.repository.SkillRepository;
import com.unihub.student.domain.repository.StudentProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentProfileUseCase Tests")
class StudentProfileUseCaseTest {

    private final UUID userId = UUID.randomUUID();
    private final UUID profileId = UUID.randomUUID();

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private GraduationCertificateRepository gradCertRepo;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private StudentProfileMapper mapper;

    @Mock
    private UniversityPartnershipApi universityPartnershipApi;

    @InjectMocks
    private StudentProfileUseCaseImpl profileUseCase;

    private StudentProfile profile;

    @BeforeEach
    void setUp() {
        profile = new StudentProfile();
        ReflectionTestUtils.setField(profile, "id", profileId);
        ReflectionTestUtils.setField(profile, "userId", userId);
        ReflectionTestUtils.setField(profile, "academicStatus", AcademicStatus.UNDERGRADUATE);
        ReflectionTestUtils.setField(profile, "certVerified", false);
        ReflectionTestUtils.setField(profile, "certificateLocked", false);
        ReflectionTestUtils.setField(profile, "skills", new HashSet<>());
    }

    @Test
    @DisplayName("should update profile fields and publish StudentProfileUpdatedEvent")
    void shouldUpdateProfileAndPublishEvent() {
        UpdateProfileRequest request = new UpdateProfileRequest("Abanoub", "A student", StudentLevel.LEVEL3, 20, "internship", 2026);
        StudentProfileResponse mockResponse = mock(StudentProfileResponse.class);

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(studentProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any())).thenReturn(mockResponse);

        StudentProfileResponse response = profileUseCase.updateProfile(userId, request);

        assertThat(response).isNotNull();

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(StudentProfileUpdatedEvent.class);

        StudentProfileUpdatedEvent event = (StudentProfileUpdatedEvent) eventCaptor.getValue();
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.profileId()).isEqualTo(profileId);
    }

    @Test
    @DisplayName("should update only non-null fields (partial update)")
    void shouldUpdateOnlyNonNullFields() {
        profile.setName("Old Name");
        profile.setBio("Old Bio");
        UpdateProfileRequest partialRequest = new UpdateProfileRequest(null, "New Bio", null, 20, null, null);

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(studentProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any())).thenReturn(mock(StudentProfileResponse.class));

        profileUseCase.updateProfile(userId, partialRequest);

        // name not overwritten since request.name() == null
        assertThat(profile.getName()).isEqualTo("Old Name");
        assertThat(profile.getBio()).isEqualTo("New Bio");
    }

    @Test
    @DisplayName("should throw InvalidOperationException when GRADUATE tries to set level")
    void shouldThrowWhenGraduateSetsLevel() {
        ReflectionTestUtils.setField(profile, "academicStatus", AcademicStatus.GRADUATE);
        UpdateProfileRequest request = new UpdateProfileRequest(null, null, StudentLevel.LEVEL3, 20, null, null);

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> profileUseCase.updateProfile(userId, request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Graduate students cannot have a level");

        verify(studentProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw NotFoundException when profile not found on update")
    void shouldThrowWhenProfileNotFoundOnUpdate() {
        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileUseCase.updateProfile(userId, new UpdateProfileRequest(null, null, null, 20, null, null)))
                .isInstanceOf(NotFoundException.class);

        verify(studentProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("should add skills to profile")
    void shouldAddSkillsToProfile() {
        UUID skillId = UUID.randomUUID();
        Skill skill = Skill.builder().id(skillId).name("Java").build();

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(skillRepository.findAllByIdIn(Set.of(skillId))).thenReturn(List.of(skill));
        when(studentProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatNoException().isThrownBy(() -> profileUseCase.updateSkills(userId, Set.of(skillId)));

        assertThat(profile.getSkills()).contains(skill);
    }

    @Test
    @DisplayName("should throw NotFoundException when profile not found on updateSkills")
    void shouldThrowWhenProfileNotFoundOnUpdateSkills() {
        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileUseCase.updateSkills(userId, Set.of()))
                .isInstanceOf(NotFoundException.class);
    }


    @Test
    @DisplayName("should upload photo and return new URL")
    void shouldUploadPhotoAndReturnUrl() {
        MockMultipartFile file = new MockMultipartFile("photo", "photo.jpg", "image/jpeg", "img".getBytes());
        profile.setProfilePhotoUrl(null);

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(fileStorageService.upload(any(), anyString())).thenReturn("https://storage/photo.jpg");
        when(studentProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String url = profileUseCase.uploadPhoto(userId, file);

        assertThat(url).isEqualTo("https://storage/photo.jpg");
        verify(fileStorageService).upload(eq(file), contains("students/photos/" + userId));
    }

    @Test
    @DisplayName("should delete old photo before uploading new one")
    void shouldDeleteOldPhotoBeforeUpload() {
        profile.setProfilePhotoUrl("https://storage/old-photo.jpg");
        MockMultipartFile newFile = new MockMultipartFile("photo", "new.jpg", "image/jpeg", "img".getBytes());

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(fileStorageService.upload(any(), anyString())).thenReturn("https://storage/new-photo.jpg");
        when(studentProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        profileUseCase.uploadPhoto(userId, newFile);

        verify(fileStorageService).delete("https://storage/old-photo.jpg");
    }

    @Test
    @DisplayName("should NOT call delete when there is no previous photo")
    void shouldNotDeleteWhenNoPreviousPhoto() {
        profile.setProfilePhotoUrl(null);
        MockMultipartFile file = new MockMultipartFile("photo", "photo.jpg", "image/jpeg", "img".getBytes());

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(fileStorageService.upload(any(), anyString())).thenReturn("https://storage/photo.jpg");
        when(studentProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        profileUseCase.uploadPhoto(userId, file);

        verify(fileStorageService, never()).delete(any());
    }

    @Test
    @DisplayName("should upload graduation certificate on first attempt successfully")
    void shouldUploadGradCertSuccessfully() {
        profile.setUniversityId(UUID.randomUUID());
        profile.setMajorId(UUID.randomUUID());
        MockMultipartFile file = new MockMultipartFile("cert", "cert.pdf", "application/pdf", "pdf".getBytes());

        GraduationCertificate savedCert = new GraduationCertificate();
        ReflectionTestUtils.setField(savedCert, "id", UUID.randomUUID());
        savedCert.setAttemptNumber(1);

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(gradCertRepo.countByStudentIdAndStatus(profileId, GraduationCertificateStatus.REJECTED)).thenReturn(0);
        when(gradCertRepo.findTopByStudentIdOrderByAttemptNumberDesc(profileId)).thenReturn(Optional.empty());
        when(gradCertRepo.save(any())).thenReturn(savedCert);
        when(fileStorageService.upload(any(), anyString())).thenReturn("https://storage/cert.pdf");

        GraduationCertResponse response = profileUseCase.uploadGraduationCertificate(userId, file);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(GraduationCertificateStatus.PENDING);
        verify(eventPublisher).publishEvent(any(GraduationCertificateSubmittedEvent.class));
    }

    @Test
    @DisplayName("should throw InvalidOperationException when file is null")
    void shouldThrowWhenFileIsNull() {
        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> profileUseCase.uploadGraduationCertificate(userId, null))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("upload a graduation certificate file");

        verify(gradCertRepo, never()).save(any());
    }

    @Test
    @DisplayName("should throw InvalidOperationException when university not set")
    void shouldThrowWhenUniversityNotSet() {
        profile.setUniversityId(null);
        profile.setMajorId(null);
        MockMultipartFile file = new MockMultipartFile("cert", "cert.pdf", "application/pdf", "pdf".getBytes());

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> profileUseCase.uploadGraduationCertificate(userId, file))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("university and major");

        verify(gradCertRepo, never()).save(any());
    }

    @Test
    @DisplayName("should throw InvalidOperationException when certificate is already approved")
    void shouldThrowWhenCertAlreadyApproved() {
        profile.setUniversityId(UUID.randomUUID());
        profile.setMajorId(UUID.randomUUID());
        ReflectionTestUtils.setField(profile, "certVerified", true);
        MockMultipartFile file = new MockMultipartFile("cert", "cert.pdf", "application/pdf", "pdf".getBytes());

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> profileUseCase.uploadGraduationCertificate(userId, file))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("already approved");

        verify(gradCertRepo, never()).save(any());
    }

    @Test
    @DisplayName("should throw InvalidOperationException when profile is locked")
    void shouldThrowWhenProfileIsLocked() {
        profile.setUniversityId(UUID.randomUUID());
        profile.setMajorId(UUID.randomUUID());
        ReflectionTestUtils.setField(profile, "certificateLocked", true);
        MockMultipartFile file = new MockMultipartFile("cert", "cert.pdf", "application/pdf", "pdf".getBytes());

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> profileUseCase.uploadGraduationCertificate(userId, file))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("maximum attempts");

        verify(gradCertRepo, never()).save(any());
    }

    @Test
    @DisplayName("should lock profile when rejection attempts reach maximum (3)")
    void shouldLockProfileAtMaxRejections() {
        profile.setUniversityId(UUID.randomUUID());
        profile.setMajorId(UUID.randomUUID());
        MockMultipartFile file = new MockMultipartFile("cert", "cert.pdf", "application/pdf", "pdf".getBytes());

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(gradCertRepo.countByStudentIdAndStatus(profileId, GraduationCertificateStatus.REJECTED)).thenReturn(3);
        when(studentProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> profileUseCase.uploadGraduationCertificate(userId, file))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("maximum attempts");

        // profile must be locked
        assertThat(profile.isCertificateLocked()).isTrue();
        verify(studentProfileRepository).save(profile);
    }

    @Test
    @DisplayName("should throw InvalidOperationException when there is already a pending certificate")
    void shouldThrowWhenPendingCertAlreadyExists() {
        profile.setUniversityId(UUID.randomUUID());
        profile.setMajorId(UUID.randomUUID());
        MockMultipartFile file = new MockMultipartFile("cert", "cert.pdf", "application/pdf", "pdf".getBytes());

        GraduationCertificate pendingCert = new GraduationCertificate();
        pendingCert.setStatus(GraduationCertificateStatus.PENDING);

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(gradCertRepo.countByStudentIdAndStatus(profileId, GraduationCertificateStatus.REJECTED)).thenReturn(0);
        when(gradCertRepo.findTopByStudentIdOrderByAttemptNumberDesc(profileId)).thenReturn(Optional.of(pendingCert));

        assertThatThrownBy(() -> profileUseCase.uploadGraduationCertificate(userId, file))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("pending");

        verify(gradCertRepo, never()).save(any());
    }

    @Test
    @DisplayName("should approve certificate and set student as GRADUATE")
    void shouldApproveCertificateAndMarkGraduate() {
        UUID universityId = UUID.randomUUID();
        UUID certId = UUID.randomUUID();
        GraduationCertificate cert = buildPendingCert(certId, universityId);

        when(gradCertRepo.findById(certId)).thenReturn(Optional.of(cert));
        when(studentProfileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(gradCertRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(studentProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        profileUseCase.reviewGraduationCertificate(certId, universityId, true, null);

        assertThat(cert.getStatus()).isEqualTo(GraduationCertificateStatus.APPROVED);
        assertThat(profile.isCertVerified()).isTrue();
        assertThat(profile.getAcademicStatus()).isEqualTo(AcademicStatus.GRADUATE);
        assertThat(profile.getLevel()).isNull();
    }

    @Test
    @DisplayName("should reject certificate with reason")
    void shouldRejectCertificateWithReason() {
        UUID universityId = UUID.randomUUID();
        UUID certId = UUID.randomUUID();
        GraduationCertificate cert = buildPendingCert(certId, universityId);

        when(gradCertRepo.findById(certId)).thenReturn(Optional.of(cert));
        when(studentProfileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(gradCertRepo.countByStudentIdAndStatus(profileId, GraduationCertificateStatus.REJECTED)).thenReturn(1);
        when(gradCertRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        profileUseCase.reviewGraduationCertificate(certId, universityId, false, "Document unclear");

        assertThat(cert.getStatus()).isEqualTo(GraduationCertificateStatus.REJECTED);
        assertThat(cert.getRejectionReason()).isEqualTo("Document unclear");
    }

    @Test
    @DisplayName("should throw InvalidOperationException when rejection reason is blank")
    void shouldThrowWhenRejectionReasonIsBlank() {
        UUID universityId = UUID.randomUUID();
        UUID certId = UUID.randomUUID();
        GraduationCertificate cert = buildPendingCert(certId, universityId);

        when(gradCertRepo.findById(certId)).thenReturn(Optional.of(cert));

        assertThatThrownBy(() -> profileUseCase.reviewGraduationCertificate(certId, universityId, false, ""))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Rejection reason is required");
    }

    @Test
    @DisplayName("should throw InvalidOperationException when reviewer is not authorized university")
    void shouldThrowWhenUnauthorizedUniversityReviews() {
        UUID certUniversityId = UUID.randomUUID();
        UUID hackerUniversityId = UUID.randomUUID();
        UUID certId = UUID.randomUUID();
        GraduationCertificate cert = buildPendingCert(certId, certUniversityId);

        when(gradCertRepo.findById(certId)).thenReturn(Optional.of(cert));

        assertThatThrownBy(() ->
                profileUseCase.reviewGraduationCertificate(certId, hackerUniversityId, true, null))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    @DisplayName("should throw InvalidOperationException when cert is already reviewed")
    void shouldThrowWhenCertAlreadyReviewed() {
        UUID universityId = UUID.randomUUID();
        UUID certId = UUID.randomUUID();
        GraduationCertificate cert = buildPendingCert(certId, universityId);
        cert.setStatus(GraduationCertificateStatus.APPROVED); // already reviewed

        when(gradCertRepo.findById(certId)).thenReturn(Optional.of(cert));

        assertThatThrownBy(() -> profileUseCase.reviewGraduationCertificate(certId, universityId, true, null))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("already been reviewed");
    }

    @Test
    @DisplayName("should lock profile after 3rd rejection")
    void shouldLockProfileOnThirdRejection() {
        UUID universityId = UUID.randomUUID();
        UUID certId = UUID.randomUUID();
        GraduationCertificate cert = buildPendingCert(certId, universityId);

        when(gradCertRepo.findById(certId)).thenReturn(Optional.of(cert));
        when(studentProfileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(gradCertRepo.countByStudentIdAndStatus(profileId, GraduationCertificateStatus.REJECTED)).thenReturn(3);
        when(gradCertRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(studentProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        profileUseCase.reviewGraduationCertificate(certId, universityId, false, "Still wrong");

        assertThat(profile.isCertificateLocked()).isTrue();
    }

    @Test
    @DisplayName("should throw NotFoundException when cert not found on review")
    void shouldThrowWhenCertNotFoundOnReview() {
        UUID certId = UUID.randomUUID();
        when(gradCertRepo.findById(certId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileUseCase.reviewGraduationCertificate(certId, UUID.randomUUID(), true, null))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Certificate not found");
    }

    @Test
    @DisplayName("should set university and major successfully on first time")
    void shouldSetUniversityOnce() {
        UUID universityId = UUID.randomUUID();
        UUID majorId = UUID.randomUUID();

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(universityPartnershipApi.isMajorOfferedByUniversity(universityId, majorId)).thenReturn(true);
        when(studentProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatNoException().isThrownBy(() -> profileUseCase.setUniversityOnce(userId, universityId, majorId));

        assertThat(profile.getUniversityId()).isEqualTo(universityId);
        assertThat(profile.getMajorId()).isEqualTo(majorId);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(StudentUniversitySetEvent.class);
    }

    @Test
    @DisplayName("should throw InvalidOperationException when university is already set")
    void shouldThrowWhenUniversityAlreadySet() {
        UUID universityId = UUID.randomUUID();
        UUID majorId = UUID.randomUUID();
        profile.setUniversityId(universityId); // already set

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> profileUseCase.setUniversityOnce(userId, universityId, majorId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("can only be set once");

        verify(studentProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw InvalidOperationException when major is not offered by the university")
    void shouldThrowWhenMajorNotOfferedByUniversity() {
        UUID universityId = UUID.randomUUID();
        UUID majorId = UUID.randomUUID();

        when(studentProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(universityPartnershipApi.isMajorOfferedByUniversity(universityId, majorId)).thenReturn(false);

        assertThatThrownBy(() -> profileUseCase.setUniversityOnce(userId, universityId, majorId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("not offered by the selected university");

        verify(studentProfileRepository, never()).save(any());
    }

    private GraduationCertificate buildPendingCert(UUID certId, UUID universityId) {
        GraduationCertificate cert = new GraduationCertificate();
        ReflectionTestUtils.setField(cert, "id", certId);
        cert.setStudentId(profileId);
        cert.setUniversityId(universityId);
        cert.setStatus(GraduationCertificateStatus.PENDING);
        return cert;
    }
}