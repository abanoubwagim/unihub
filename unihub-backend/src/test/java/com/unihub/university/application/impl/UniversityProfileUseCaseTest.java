package com.unihub.university.application.impl;

import com.unihub.shared.exception.NotFoundException;
import com.unihub.shared.storage.FileStorageService;
import com.unihub.university.api.dto.req.UpdateProfileRequest;
import com.unihub.university.api.dto.res.UniversityProfileResponse;
import com.unihub.university.application.UniversityProfileMapper;
import com.unihub.university.domain.model.UniversityProfile;
import com.unihub.university.domain.repository.UniversityProfileRepository;
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
@DisplayName("UniversityProfileUseCase Tests")
class UniversityProfileUseCaseTest {

    private final UUID userId = UUID.randomUUID();
    private final UUID profileId = UUID.randomUUID();

    @Mock
    private UniversityProfileRepository universityProfileRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private UniversityProfileMapper mapper;

    @InjectMocks
    private UniversityProfileUseCaseImpl profileUseCase;

    private UniversityProfile profile;

    @BeforeEach
    void setUp() {
        profile = UniversityProfile.builder().userId(userId).name("State University").build();
        ReflectionTestUtils.setField(profile, "id", profileId);
    }

    @Test
    @DisplayName("should return profile response for the authenticated university user")
    void shouldReturnMyProfile() {
        UniversityProfileResponse mockResponse = mock(UniversityProfileResponse.class);
        when(universityProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(mapper.toResponse(profile)).thenReturn(mockResponse);

        UniversityProfileResponse response = profileUseCase.getMyProfile(userId);

        assertThat(response).isNotNull();
        verify(mapper).toResponse(profile);
    }

    @Test
    @DisplayName("should throw NotFoundException when profile not found on getMyProfile")
    void shouldThrowWhenProfileNotFoundOnGet() {
        when(universityProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileUseCase.getMyProfile(userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("University profile not found");
    }

    @Test
    @DisplayName("should update all non-null fields of the university profile")
    void shouldUpdateAllNonNullFields() {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "New Name", "New Bio", "https://university.edu", "123 Main St", 1);
        UniversityProfileResponse mockResponse = mock(UniversityProfileResponse.class);

        when(universityProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(universityProfileRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toResponse(any())).thenReturn(mockResponse);

        profileUseCase.updateProfile(userId, request);

        assertThat(profile.getName()).isEqualTo("New Name");
        assertThat(profile.getBio()).isEqualTo("New Bio");
        assertThat(profile.getWebsiteUrl()).isEqualTo("https://university.edu");
        assertThat(profile.getAddress()).isEqualTo("123 Main St");
        assertThat(profile.getCountryId()).isEqualTo(1);
    }

    @Test
    @DisplayName("should only update non-null fields (partial update)")
    void shouldOnlyUpdateNonNullFields() {
        profile.setName("Original Name");
        profile.setAddress("Original Address");
        UpdateProfileRequest partial = new UpdateProfileRequest(null, "New Bio", null, null, 2);
        UniversityProfileResponse mockResponse = mock(UniversityProfileResponse.class);

        when(universityProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(universityProfileRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toResponse(any())).thenReturn(mockResponse);

        profileUseCase.updateProfile(userId, partial);

        assertThat(profile.getName()).isEqualTo("Original Name");
        assertThat(profile.getBio()).isEqualTo("New Bio");
        assertThat(profile.getAddress()).isEqualTo("Original Address");
    }

    @Test
    @DisplayName("should save and return updated profile response")
    void shouldSaveAndReturnResponse() {
        UpdateProfileRequest request = new UpdateProfileRequest("Updated", null, null, null, 1);
        UniversityProfileResponse mockResponse = mock(UniversityProfileResponse.class);

        when(universityProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(universityProfileRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toResponse(any())).thenReturn(mockResponse);

        UniversityProfileResponse response = profileUseCase.updateProfile(userId, request);

        assertThat(response).isNotNull();
        verify(universityProfileRepository).save(profile);
    }

    @Test
    @DisplayName("should throw NotFoundException when profile not found on update")
    void shouldThrowWhenProfileNotFoundOnUpdate() {
        when(universityProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileUseCase.updateProfile(userId,
                new UpdateProfileRequest(null, null, null, null, 1)))
                .isInstanceOf(NotFoundException.class);

        verify(universityProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("should upload photo and return the new URL")
    void shouldUploadPhotoAndReturnUrl() {
        profile.setProfilePhotoUrl(null);
        MockMultipartFile file = new MockMultipartFile("file", "logo.png", "image/png", "img".getBytes());

        when(universityProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(fileStorageService.upload(any(), anyString())).thenReturn("https://storage/logo.png");
        when(universityProfileRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        String url = profileUseCase.uploadPhoto(userId, file);

        assertThat(url).isEqualTo("https://storage/logo.png");
        verify(fileStorageService).upload(eq(file), contains("universities/photos/" + userId));
        assertThat(profile.getProfilePhotoUrl()).isEqualTo("https://storage/logo.png");
    }

    @Test
    @DisplayName("should delete old photo before uploading a new one")
    void shouldDeleteOldPhotoBeforeUpload() {
        profile.setProfilePhotoUrl("https://storage/old-logo.png");
        MockMultipartFile newFile = new MockMultipartFile("file", "new.png", "image/png", "img".getBytes());

        when(universityProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(fileStorageService.upload(any(), anyString())).thenReturn("https://storage/new-logo.png");
        when(universityProfileRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        profileUseCase.uploadPhoto(userId, newFile);

        verify(fileStorageService).delete("https://storage/old-logo.png");
    }

    @Test
    @DisplayName("should NOT call delete when there is no previous photo")
    void shouldNotDeleteWhenNoPreviousPhoto() {
        profile.setProfilePhotoUrl(null);
        MockMultipartFile file = new MockMultipartFile("file", "logo.png", "image/png", "img".getBytes());

        when(universityProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(fileStorageService.upload(any(), anyString())).thenReturn("https://storage/logo.png");
        when(universityProfileRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        profileUseCase.uploadPhoto(userId, file);

        verify(fileStorageService, never()).delete(any());
    }

    @Test
    @DisplayName("should NOT call delete when previous photo URL is blank")
    void shouldNotDeleteWhenPreviousPhotoIsBlank() {
        profile.setProfilePhotoUrl("   ");
        MockMultipartFile file = new MockMultipartFile("file", "logo.png", "image/png", "img".getBytes());

        when(universityProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(fileStorageService.upload(any(), anyString())).thenReturn("https://storage/logo.png");
        when(universityProfileRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        profileUseCase.uploadPhoto(userId, file);

        verify(fileStorageService, never()).delete(any());
    }

    @Test
    @DisplayName("should throw NotFoundException when profile not found on uploadPhoto")
    void shouldThrowWhenProfileNotFoundOnPhotoUpload() {
        when(universityProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        MockMultipartFile file = new MockMultipartFile("file", "logo.png", "image/png", "img".getBytes());

        assertThatThrownBy(() -> profileUseCase.uploadPhoto(userId, file))
                .isInstanceOf(NotFoundException.class);

        verify(fileStorageService, never()).upload(any(), any());
    }
}