package com.unihub.company.application.impl;

import com.unihub.company.api.dto.req.UpdateProfileRequest;
import com.unihub.company.api.dto.res.CompanyProfileResponse;
import com.unihub.company.application.mapper.CompanyProfileMapper;
import com.unihub.company.domain.model.CompanyProfile;
import com.unihub.company.domain.repository.CompanyProfileRepository;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.shared.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyProfileUseCase Tests")
class CompanyProfileUseCaseTest {

    private final UUID userId = UUID.randomUUID();
    private final UUID profileId = UUID.randomUUID();

    @Mock
    private CompanyProfileRepository companyProfileRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private CompanyProfileMapper mapper;

    @InjectMocks
    private CompanyProfileUseCaseImpl profileUseCase;

    private CompanyProfile profile;

    @BeforeEach
    void setUp() {
        profile = CompanyProfile.builder()
                .userId(userId)
                .name("Acme Corp")
                .build();
        ReflectionTestUtils.setField(profile, "id", profileId);
    }

    @Test
    @DisplayName("should return profile response for authenticated company user")
    void shouldReturnMyProfile() {
        CompanyProfileResponse mockResponse = mock(CompanyProfileResponse.class);
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(mapper.toResponse(profile)).thenReturn(mockResponse);

        CompanyProfileResponse response = profileUseCase.getMyProfile(userId);

        assertThat(response).isNotNull();
        verify(mapper).toResponse(profile);
    }

    @Test
    @DisplayName("should throw NotFoundException when profile not found on getMyProfile")
    void shouldThrowWhenProfileNotFoundOnGet() {
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileUseCase.getMyProfile(userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Company profile not found");
    }

    @Test
    @DisplayName("should update all non-null fields of the company profile")
    void shouldUpdateAllNonNullFields() {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "New Name", "New Description", "https://acme.com", 1, "Fintech");
        CompanyProfileResponse mockResponse = mock(CompanyProfileResponse.class);

        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(companyProfileRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toResponse(any())).thenReturn(mockResponse);

        profileUseCase.updateProfile(userId, request);

        assertThat(profile.getName()).isEqualTo("New Name");
        assertThat(profile.getDescription()).isEqualTo("New Description");
        assertThat(profile.getWebsiteUrl()).isEqualTo("https://acme.com");
        assertThat(profile.getCountryId()).isEqualTo(1);
        assertThat(profile.getSpecialization()).isEqualTo("Fintech");
    }

    @Test
    @DisplayName("should only update non-null fields (partial update)")
    void shouldOnlyUpdateNonNullFields() {
        profile.setName("Original Name");
        profile.setSpecialization("OldSpec");
        UpdateProfileRequest partial = new UpdateProfileRequest(null, "New Bio", null, 2, null);
        CompanyProfileResponse mockResponse = mock(CompanyProfileResponse.class);

        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(companyProfileRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toResponse(any())).thenReturn(mockResponse);

        profileUseCase.updateProfile(userId, partial);

        assertThat(profile.getName()).isEqualTo("Original Name");
        assertThat(profile.getDescription()).isEqualTo("New Bio");
        assertThat(profile.getSpecialization()).isEqualTo("OldSpec");
    }

    @Test
    @DisplayName("should save and return updated profile response")
    void shouldSaveAndReturnUpdatedProfile() {
        UpdateProfileRequest request = new UpdateProfileRequest("Updated", null, null, 1, null);
        CompanyProfileResponse mockResponse = mock(CompanyProfileResponse.class);

        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(companyProfileRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toResponse(any())).thenReturn(mockResponse);

        CompanyProfileResponse response = profileUseCase.updateProfile(userId, request);

        assertThat(response).isNotNull();
        verify(companyProfileRepository).save(profile);
    }

    @Test
    @DisplayName("should throw NotFoundException when profile not found on update")
    void shouldThrowWhenProfileNotFoundOnUpdate() {
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileUseCase.updateProfile(userId,
                new UpdateProfileRequest(null, null, null, 1, null)))
                .isInstanceOf(NotFoundException.class);

        verify(companyProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("should upload photo and return the new URL")
    void shouldUploadPhotoAndReturnUrl() {
        profile.setProfilePhotoUrl(null);
        MockMultipartFile file = new MockMultipartFile("file", "logo.png", "image/png", "img".getBytes());

        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(fileStorageService.upload(any(), anyString())).thenReturn("https://storage/logo.png");
        when(companyProfileRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        String url = profileUseCase.uploadPhoto(userId, file);

        assertThat(url).isEqualTo("https://storage/logo.png");
        verify(fileStorageService).upload(eq(file), contains("companies/photos/" + userId));
        assertThat(profile.getProfilePhotoUrl()).isEqualTo("https://storage/logo.png");
    }

    @Test
    @DisplayName("should delete old photo before uploading a new one")
    void shouldDeleteOldPhotoBeforeUpload() {
        profile.setProfilePhotoUrl("https://storage/old-logo.png");
        MockMultipartFile newFile = new MockMultipartFile("file", "new.png", "image/png", "img".getBytes());

        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(fileStorageService.upload(any(), anyString())).thenReturn("https://storage/new-logo.png");
        when(companyProfileRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        profileUseCase.uploadPhoto(userId, newFile);

        verify(fileStorageService).delete("https://storage/old-logo.png");
    }

    @Test
    @DisplayName("should NOT call delete when there is no previous photo")
    void shouldNotDeleteWhenNoPreviousPhoto() {
        profile.setProfilePhotoUrl(null);
        MockMultipartFile file = new MockMultipartFile("file", "logo.png", "image/png", "img".getBytes());

        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(fileStorageService.upload(any(), anyString())).thenReturn("https://storage/logo.png");
        when(companyProfileRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        profileUseCase.uploadPhoto(userId, file);

        verify(fileStorageService, never()).delete(any());
    }

    @Test
    @DisplayName("should NOT call delete when previous photo URL is blank")
    void shouldNotDeleteWhenPreviousPhotoIsBlank() {
        profile.setProfilePhotoUrl("   ");
        MockMultipartFile file = new MockMultipartFile("file", "logo.png", "image/png", "img".getBytes());

        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(fileStorageService.upload(any(), anyString())).thenReturn("https://storage/logo.png");
        when(companyProfileRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        profileUseCase.uploadPhoto(userId, file);

        verify(fileStorageService, never()).delete(any());
    }

    @Test
    @DisplayName("should throw NotFoundException when profile not found on uploadPhoto")
    void shouldThrowWhenProfileNotFoundOnPhotoUpload() {
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        MockMultipartFile file = new MockMultipartFile("file", "logo.png", "image/png", "img".getBytes());

        assertThatThrownBy(() -> profileUseCase.uploadPhoto(userId, file))
                .isInstanceOf(NotFoundException.class);

        verify(fileStorageService, never()).upload(any(), any());
    }
}