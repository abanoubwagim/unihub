package com.unihub.company.application.impl;

import com.unihub.company.api.dto.req.CreatePartnershipRequest;
import com.unihub.company.api.dto.req.ReviewPartnershipRequest;
import com.unihub.company.api.dto.res.PartnershipResponse;
import com.unihub.company.domain.model.CompanyProfile;
import com.unihub.company.domain.repository.CompanyProfileRepository;
import com.unihub.shared.api.dto.PageResponse;
import com.unihub.shared.api.dto.external.PartnershipRecord;
import com.unihub.shared.api.external.UniversityPartnershipApi;
import com.unihub.shared.domain.enums.PartnershipRequester;
import com.unihub.shared.domain.enums.PartnershipStatus;
import com.unihub.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyPartnershipUseCase Tests")
class CompanyPartnershipUseCaseTest {

    private final UUID userId = UUID.randomUUID();
    private final UUID companyId = UUID.randomUUID();
    private final UUID universityId = UUID.randomUUID();
    private final UUID partnershipId = UUID.randomUUID();

    @Mock
    private CompanyProfileRepository companyProfileRepository;

    @Mock
    private UniversityPartnershipApi universityPartnershipApi;

    @InjectMocks
    private CompanyPartnershipUseCaseImpl partnershipUseCase;

    private CompanyProfile profile;

    @BeforeEach
    void setUp() {
        profile = CompanyProfile.builder().userId(userId).name("Acme Corp").build();
        ReflectionTestUtils.setField(profile, "id", companyId);
    }

    @Test
    @DisplayName("should return paginated partnerships for the company")
    void shouldReturnPaginatedPartnerships() {
        Pageable pageable = PageRequest.of(0, 20);
        PartnershipRecord record = buildRecord(PartnershipStatus.ACTIVE);
        Page<PartnershipRecord> page = new PageImpl<>(List.of(record), pageable, 1);

        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(universityPartnershipApi.getAllForCompany(companyId, pageable)).thenReturn(page);

        PageResponse<PartnershipResponse> response = partnershipUseCase.getAll(userId, pageable);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).partnershipId()).isEqualTo(partnershipId);
        assertThat(response.content().get(0).status()).isEqualTo(PartnershipStatus.ACTIVE);
    }

    @Test
    @DisplayName("should return empty page when no partnerships exist")
    void shouldReturnEmptyPageWhenNoPartnerships() {
        Pageable pageable = PageRequest.of(0, 20);
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(universityPartnershipApi.getAllForCompany(companyId, pageable))
                .thenReturn(Page.empty(pageable));

        PageResponse<PartnershipResponse> response = partnershipUseCase.getAll(userId, pageable);

        assertThat(response.content()).isEmpty();
    }

    @Test
    @DisplayName("should throw NotFoundException when profile not found on getAll")
    void shouldThrowWhenProfileNotFoundOnGetAll() {
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> partnershipUseCase.getAll(userId, PageRequest.of(0, 20)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Company profile not found");
    }

    @Test
    @DisplayName("should request a partnership successfully")
    void shouldRequestPartnershipSuccessfully() {
        PartnershipRecord record = buildRecord(PartnershipStatus.PENDING);

        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(universityPartnershipApi.requestPartnership(companyId, universityId)).thenReturn(record);

        PartnershipResponse response = partnershipUseCase.requestPartnership(
                userId, new CreatePartnershipRequest(universityId));

        assertThat(response.partnershipId()).isEqualTo(partnershipId);
        assertThat(response.status()).isEqualTo(PartnershipStatus.PENDING);
        assertThat(response.universityProfileId()).isEqualTo(universityId);
        verify(universityPartnershipApi).requestPartnership(companyId, universityId);
    }

    @Test
    @DisplayName("should throw NotFoundException when profile not found on requestPartnership")
    void shouldThrowWhenProfileNotFoundOnRequest() {
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> partnershipUseCase.requestPartnership(
                userId, new CreatePartnershipRequest(universityId)))
                .isInstanceOf(NotFoundException.class);

        verify(universityPartnershipApi, never()).requestPartnership(any(), any());
    }

    @Test
    @DisplayName("should propagate exception from UniversityPartnershipApi on requestPartnership")
    void shouldPropagateExceptionFromApiOnRequest() {
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(universityPartnershipApi.requestPartnership(companyId, universityId))
                .thenThrow(new com.unihub.shared.exception.InvalidOperationException("Already exists"));

        assertThatThrownBy(() -> partnershipUseCase.requestPartnership(
                userId, new CreatePartnershipRequest(universityId)))
                .isInstanceOf(com.unihub.shared.exception.InvalidOperationException.class)
                .hasMessageContaining("Already exists");
    }

    @Test
    @DisplayName("should accept a partnership review successfully")
    void shouldAcceptPartnershipReview() {
        PartnershipRecord accepted = buildRecord(PartnershipStatus.ACTIVE);

        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(universityPartnershipApi.reviewPartnership(companyId, partnershipId, true))
                .thenReturn(accepted);

        PartnershipResponse response = partnershipUseCase.reviewPartnership(
                userId, partnershipId, new ReviewPartnershipRequest(true));

        assertThat(response.status()).isEqualTo(PartnershipStatus.ACTIVE);
        verify(universityPartnershipApi).reviewPartnership(companyId, partnershipId, true);
    }

    @Test
    @DisplayName("should reject a partnership review successfully")
    void shouldRejectPartnershipReview() {
        PartnershipRecord rejected = buildRecord(PartnershipStatus.REJECTED);

        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(universityPartnershipApi.reviewPartnership(companyId, partnershipId, false))
                .thenReturn(rejected);

        PartnershipResponse response = partnershipUseCase.reviewPartnership(
                userId, partnershipId, new ReviewPartnershipRequest(false));

        assertThat(response.status()).isEqualTo(PartnershipStatus.REJECTED);
    }

    @Test
    @DisplayName("should throw NotFoundException when profile not found on reviewPartnership")
    void shouldThrowWhenProfileNotFoundOnReview() {
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> partnershipUseCase.reviewPartnership(
                userId, partnershipId, new ReviewPartnershipRequest(true)))
                .isInstanceOf(NotFoundException.class);

        verify(universityPartnershipApi, never()).reviewPartnership(any(), any(), anyBoolean());
    }

    @Test
    @DisplayName("should terminate a partnership successfully")
    void shouldTerminatePartnershipSuccessfully() {
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        doNothing().when(universityPartnershipApi).terminatePartnership(companyId, partnershipId);

        assertThatNoException().isThrownBy(
                () -> partnershipUseCase.terminate(userId, partnershipId));

        verify(universityPartnershipApi).terminatePartnership(companyId, partnershipId);
    }

    @Test
    @DisplayName("should throw NotFoundException when profile not found on terminate")
    void shouldThrowWhenProfileNotFoundOnTerminate() {
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> partnershipUseCase.terminate(userId, partnershipId))
                .isInstanceOf(NotFoundException.class);

        verify(universityPartnershipApi, never()).terminatePartnership(any(), any());
    }

    @Test
    @DisplayName("should propagate exception from UniversityPartnershipApi on terminate")
    void shouldPropagateExceptionFromApiOnTerminate() {
        when(companyProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        doThrow(new com.unihub.shared.exception.InvalidOperationException("Not ACTIVE"))
                .when(universityPartnershipApi).terminatePartnership(companyId, partnershipId);

        assertThatThrownBy(() -> partnershipUseCase.terminate(userId, partnershipId))
                .isInstanceOf(com.unihub.shared.exception.InvalidOperationException.class)
                .hasMessageContaining("Not ACTIVE");
    }

    private PartnershipRecord buildRecord(PartnershipStatus status) {
        return new PartnershipRecord(
                partnershipId, universityId, companyId,
                status, PartnershipRequester.COMPANY, LocalDateTime.now());
    }
}