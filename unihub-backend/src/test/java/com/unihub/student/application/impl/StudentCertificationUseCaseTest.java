package com.unihub.student.application.impl;

import com.unihub.shared.api.dto.PageResponse;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.shared.storage.FileStorageService;
import com.unihub.student.api.dto.req.CertificationRequest;
import com.unihub.student.api.dto.res.CertificationResponse;
import com.unihub.student.domain.model.StudentCertification;
import com.unihub.student.domain.model.StudentProfile;
import com.unihub.student.domain.repository.StudentCertificationRepository;
import com.unihub.student.domain.repository.StudentProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentCertificationUseCase Tests")
class StudentCertificationUseCaseTest {

    private final UUID userId = UUID.randomUUID();
    private final UUID certId = UUID.randomUUID();

    @Mock
    private StudentProfileRepository profileRepository;

    @Mock
    private StudentCertificationRepository certificationRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private StudentCertificationUseCaseImpl certificationUseCase;

    private StudentProfile profile;
    private CertificationRequest validRequest;

    @BeforeEach
    void setUp() {
        profile = new StudentProfile();
        ReflectionTestUtils.setField(profile, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(profile, "userId", userId);

        validRequest = new CertificationRequest(
                "AWS Certified Developer",
                "Amazon Web Services",
                LocalDate.of(2024, 6, 15));
    }

    @Test
    @DisplayName("should add certification without file successfully")
    void shouldAddCertificationWithoutFile() {
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(certificationRepository.save(any())).thenAnswer(inv -> {
            StudentCertification c = inv.getArgument(0);
            ReflectionTestUtils.setField(c, "id", certId);
            return c;
        });

        CertificationResponse response = certificationUseCase.add(userId, validRequest, null);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("AWS Certified Developer");
        assertThat(response.issuingOrganization()).isEqualTo("Amazon Web Services");
        assertThat(response.dateIssued()).isEqualTo(LocalDate.of(2024, 6, 15));
        assertThat(response.fileUrl()).isNull();

        verify(fileStorageService, never()).upload(any(), any());
    }

    @Test
    @DisplayName("should add certification with file and upload it")
    void shouldAddCertificationWithFile() {
        MultipartFile file = new MockMultipartFile("file", "cert.pdf", "application/pdf", "pdf-content".getBytes());

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(fileStorageService.upload(any(), anyString())).thenReturn("https://storage/cert.pdf");
        when(certificationRepository.save(any())).thenAnswer(inv -> {
            StudentCertification c = inv.getArgument(0);
            ReflectionTestUtils.setField(c, "id", certId);
            return c;
        });

        CertificationResponse response = certificationUseCase.add(userId, validRequest, file);

        assertThat(response.fileUrl()).isEqualTo("https://storage/cert.pdf");
        verify(fileStorageService).upload(eq(file), contains("students/certifications/" + userId));
    }


    @Test
    @DisplayName("should not upload file when file is empty")
    void shouldNotUploadWhenFileIsEmpty() {
        MultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(certificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        certificationUseCase.add(userId, validRequest, emptyFile);

        verify(fileStorageService, never()).upload(any(), any());
    }

    @Test
    @DisplayName("should throw NotFoundException when student profile not found on add")
    void shouldThrowWhenProfileNotFoundOnAdd() {
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificationUseCase.add(userId, validRequest, null))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Student profile not found");

        verify(certificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("should save certification linked to the correct student profile")
    void shouldSaveCertificationLinkedToProfile() {
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(certificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        certificationUseCase.add(userId, validRequest, null);

        ArgumentCaptor<StudentCertification> captor = ArgumentCaptor.forClass(StudentCertification.class);
        verify(certificationRepository).save(captor.capture());
        assertThat(captor.getValue().getStudent()).isSameAs(profile);
    }

    @Test
    @DisplayName("should update certification fields without changing file")
    void shouldUpdateCertificationFieldsWithoutFile() {
        StudentCertification existing = buildCert("Old Title", "Old Org", null);
        CertificationRequest updateReq = new CertificationRequest("New Title", "New Org", LocalDate.of(2025, 1, 1));

        when(certificationRepository.findByIdAndStudent_UserId(certId, userId)).thenReturn(Optional.of(existing));
        when(certificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CertificationResponse response = certificationUseCase.update(userId, certId, updateReq, null);

        assertThat(response.title()).isEqualTo("New Title");
        assertThat(response.issuingOrganization()).isEqualTo("New Org");
        assertThat(response.dateIssued()).isEqualTo(LocalDate.of(2025, 1, 1));
        verify(fileStorageService, never()).upload(any(), any());
        verify(fileStorageService, never()).delete(any());
    }

    @Test
    @DisplayName("should replace old file and delete it when new file is provided")
    void shouldReplaceOldFileOnUpdate() {
        StudentCertification existing = buildCert("Title", "Org", "https://old-url/cert.pdf");
        MultipartFile newFile = new MockMultipartFile("file", "new.pdf", "application/pdf", "content".getBytes());

        when(certificationRepository.findByIdAndStudent_UserId(certId, userId)).thenReturn(Optional.of(existing));
        when(fileStorageService.upload(any(), anyString())).thenReturn("https://new-url/cert.pdf");
        when(certificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CertificationResponse response = certificationUseCase.update(userId, certId, validRequest, newFile);

        assertThat(response.fileUrl()).isEqualTo("https://new-url/cert.pdf");
        verify(fileStorageService).delete("https://old-url/cert.pdf");
        verify(fileStorageService).upload(eq(newFile), anyString());
    }

    @Test
    @DisplayName("should NOT delete old file when cert had no previous file on update")
    void shouldNotDeleteWhenNoPreviousFile() {
        StudentCertification existing = buildCert("Title", "Org", null); // no old file
        MultipartFile newFile = new MockMultipartFile("file", "new.pdf", "application/pdf", "content".getBytes());

        when(certificationRepository.findByIdAndStudent_UserId(certId, userId)).thenReturn(Optional.of(existing));
        when(fileStorageService.upload(any(), anyString())).thenReturn("https://new-url/cert.pdf");
        when(certificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        certificationUseCase.update(userId, certId, validRequest, newFile);

        verify(fileStorageService, never()).delete(any());
    }

    @Test
    @DisplayName("should throw NotFoundException when cert not found on update")
    void shouldThrowWhenCertNotFoundOnUpdate() {
        when(certificationRepository.findByIdAndStudent_UserId(certId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificationUseCase.update(userId, certId, validRequest, null))
                .isInstanceOf(NotFoundException.class);

        verify(certificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("should delete certification and its file when file exists")
    void shouldDeleteCertificationWithFile() {
        StudentCertification cert = buildCert("Title", "Org", "https://storage/cert.pdf");
        when(certificationRepository.findByIdAndStudent_UserId(certId, userId)).thenReturn(Optional.of(cert));

        assertThatNoException().isThrownBy(() -> certificationUseCase.delete(userId, certId));

        verify(certificationRepository).delete(cert);
        verify(fileStorageService).delete("https://storage/cert.pdf");
    }

    @Test
    @DisplayName("should delete certification without deleting file when cert has no file")
    void shouldDeleteCertificationWithoutFile() {
        StudentCertification cert = buildCert("Title", "Org", null);
        when(certificationRepository.findByIdAndStudent_UserId(certId, userId)).thenReturn(Optional.of(cert));

        certificationUseCase.delete(userId, certId);

        verify(certificationRepository).delete(cert);
        verify(fileStorageService, never()).delete(any());
    }

    @Test
    @DisplayName("should throw NotFoundException when cert not found on delete")
    void shouldThrowWhenCertNotFoundOnDelete() {
        when(certificationRepository.findByIdAndStudent_UserId(certId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificationUseCase.delete(userId, certId))
                .isInstanceOf(NotFoundException.class);

        verify(certificationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("should return paginated certifications for the user")
    void shouldReturnPaginatedCertifications() {
        Pageable pageable = PageRequest.of(0, 10);
        StudentCertification cert = buildCert("AWS", "Amazon", null);
        ReflectionTestUtils.setField(cert, "id", certId);

        Page<StudentCertification> page = new PageImpl<>(List.of(cert), pageable, 1);
        when(certificationRepository.findAllByStudent_UserId(userId, pageable)).thenReturn(page);

        PageResponse<CertificationResponse> response = certificationUseCase.getAll(userId, pageable);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).title()).isEqualTo("AWS");
        assertThat(response.totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("should return empty page when user has no certifications")
    void shouldReturnEmptyPageWhenNoCertifications() {
        Pageable pageable = PageRequest.of(0, 10);
        when(certificationRepository.findAllByStudent_UserId(userId, pageable))
                .thenReturn(Page.empty(pageable));

        PageResponse<CertificationResponse> response = certificationUseCase.getAll(userId, pageable);

        assertThat(response.content()).isEmpty();
    }

    @Test
    @DisplayName("should return single certification by id")
    void shouldReturnSingleCertification() {
        StudentCertification cert = buildCert("AWS", "Amazon", "https://url");
        ReflectionTestUtils.setField(cert, "id", certId);

        when(certificationRepository.findByIdAndStudent_UserId(certId, userId)).thenReturn(Optional.of(cert));

        CertificationResponse response = certificationUseCase.getOne(userId, certId);

        assertThat(response.id()).isEqualTo(certId);
        assertThat(response.title()).isEqualTo("AWS");
        assertThat(response.fileUrl()).isEqualTo("https://url");
    }

    @Test
    @DisplayName("should throw NotFoundException when certification not found by id")
    void shouldThrowWhenCertNotFoundById() {
        when(certificationRepository.findByIdAndStudent_UserId(certId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificationUseCase.getOne(userId, certId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Certification not found");
    }

    private StudentCertification buildCert(String title, String org, String fileUrl) {
        StudentCertification cert = StudentCertification.builder()
                .student(profile)
                .title(title)
                .issuingOrganization(org)
                .dateIssued(LocalDate.of(2024, 1, 1))
                .fileUrl(fileUrl)
                .build();
        ReflectionTestUtils.setField(cert, "id", certId);
        return cert;
    }
}