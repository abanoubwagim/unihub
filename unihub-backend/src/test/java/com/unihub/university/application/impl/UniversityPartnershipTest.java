package com.unihub.university.application.impl;

import com.unihub.shared.api.dto.external.PartnershipRecord;
import com.unihub.shared.domain.enums.PartnershipRequester;
import com.unihub.shared.domain.enums.PartnershipStatus;
import com.unihub.shared.exception.InvalidOperationException;
import com.unihub.shared.exception.NotFoundException;
import com.unihub.university.domain.event.UniversityPartnershipAcceptedEvent;
import com.unihub.university.domain.event.UniversityPartnershipRequestedEvent;
import com.unihub.university.domain.model.UniversityPartnership;
import com.unihub.university.domain.repository.UniversityPartnershipRepository;
import com.unihub.university.domain.repository.UniversityProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UniversityPartnershipApiImpl Tests")
class UniversityPartnershipApiImplTest {

    private final UUID universityId = UUID.randomUUID();
    private final UUID companyId = UUID.randomUUID();
    private final UUID partnershipId = UUID.randomUUID();

    @Mock
    private UniversityProfileRepository universityProfileRepository;

    @Mock
    private UniversityPartnershipRepository partnershipRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UniversityPartnershipApiImpl partnershipApi;

    private UniversityPartnership buildPartnership(PartnershipStatus status,
                                                   PartnershipRequester requester) {
        UniversityPartnership p = UniversityPartnership.builder()
                .universityId(universityId)
                .companyId(companyId)
                .status(status)
                .requestedBy(requester)
                .build();
        ReflectionTestUtils.setField(p, "id", partnershipId);
        ReflectionTestUtils.setField(p, "createdAt", LocalDateTime.now());
        return p;
    }

    @Test
    @DisplayName("should request partnership from company side and publish event")
    void shouldRequestPartnershipFromCompanySide() {
        when(universityProfileRepository.existsById(universityId)).thenReturn(true);
        when(partnershipRepository.existsByUniversityIdAndCompanyId(
                universityId, companyId)).thenReturn(false);
        when(partnershipRepository.save(any())).thenAnswer(inv -> {
            UniversityPartnership saved = inv.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", partnershipId);
            ReflectionTestUtils.setField(saved, "createdAt", LocalDateTime.now());
            return saved;
        });

        PartnershipRecord record = partnershipApi.requestPartnership(companyId, universityId);

        assertThat(record.requestedBy()).isEqualTo(PartnershipRequester.COMPANY);
        assertThat(record.status()).isEqualTo(PartnershipStatus.PENDING);
        verify(eventPublisher).publishEvent(any(UniversityPartnershipRequestedEvent.class));
    }

    @Test
    @DisplayName("should throw NotFoundException when university profile does not exist")
    void shouldThrowWhenUniversityNotFoundOnCompanyRequest() {
        when(universityProfileRepository.existsById(universityId)).thenReturn(false);

        assertThatThrownBy(() -> partnershipApi.requestPartnership(companyId, universityId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("University profile not found");
    }

    @Test
    @DisplayName("should return active partner company IDs for a university")
    void shouldReturnActivePartnerCompanyIds() {
        when(partnershipRepository.findActivePartnerCompanyIds(universityId))
                .thenReturn(List.of(companyId, UUID.randomUUID()));

        Set<UUID> ids = partnershipApi.getActivePartnerCompanyIds(universityId);

        assertThat(ids).hasSize(2).contains(companyId);
    }

    @Test
    @DisplayName("should return empty set when university has no active partners")
    void shouldReturnEmptySetWhenNoActivePartners() {
        when(partnershipRepository.findActivePartnerCompanyIds(universityId))
                .thenReturn(List.of());

        Set<UUID> ids = partnershipApi.getActivePartnerCompanyIds(universityId);

        assertThat(ids).isEmpty();
    }

    @Test
    @DisplayName("should delegate isMajorOfferedByUniversity to the profile repository")
    void shouldDelegateIsMajorOfferedCheck() {
        UUID majorId = UUID.randomUUID();
        when(universityProfileRepository.existsByIdAndMajors_Id(universityId, majorId))
                .thenReturn(true);

        assertThat(partnershipApi.isMajorOfferedByUniversity(universityId, majorId)).isTrue();
    }

    @Test
    @DisplayName("should return false when major is not offered by the university")
    void shouldReturnFalseWhenMajorNotOffered() {
        UUID majorId = UUID.randomUUID();
        when(universityProfileRepository.existsByIdAndMajors_Id(universityId, majorId))
                .thenReturn(false);

        assertThat(partnershipApi.isMajorOfferedByUniversity(universityId, majorId)).isFalse();
    }

    @Test
    @DisplayName("should accept company review for a UNIVERSITY-requested partnership")
    void shouldAcceptPartnershipFromCompanySide() {
        UniversityPartnership pending =
                buildPartnership(PartnershipStatus.PENDING, PartnershipRequester.UNIVERSITY);
        when(partnershipRepository.findByIdAndCompanyId(partnershipId, companyId))
                .thenReturn(Optional.of(pending));
        when(partnershipRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PartnershipRecord result = partnershipApi.reviewPartnership(companyId, partnershipId, true);

        assertThat(result.status()).isEqualTo(PartnershipStatus.ACTIVE);
        verify(eventPublisher).publishEvent(any(UniversityPartnershipAcceptedEvent.class));
    }

    @Test
    @DisplayName("should throw when company reviews its own request")
    void shouldThrowWhenCompanyReviewsOwnRequest() {
        UniversityPartnership ownRequest =
                buildPartnership(PartnershipStatus.PENDING, PartnershipRequester.COMPANY);
        when(partnershipRepository.findByIdAndCompanyId(partnershipId, companyId))
                .thenReturn(Optional.of(ownRequest));

        assertThatThrownBy(() -> partnershipApi.reviewPartnership(companyId, partnershipId, true))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("you requested");
    }

    @Test
    @DisplayName("should throw when terminating a non-ACTIVE partnership from company side")
    void shouldThrowWhenTerminatingNonActiveFromCompanySide() {
        UniversityPartnership pending =
                buildPartnership(PartnershipStatus.PENDING, PartnershipRequester.UNIVERSITY);
        when(partnershipRepository.findByIdAndCompanyId(partnershipId, companyId))
                .thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> partnershipApi.terminatePartnership(companyId, partnershipId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("ACTIVE");
    }
}