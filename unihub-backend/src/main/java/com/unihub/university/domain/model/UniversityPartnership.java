package com.unihub.university.domain.model;

import com.unihub.shared.domain.enums.PartnershipRequester;
import com.unihub.shared.domain.enums.PartnershipStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "university_partnerships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"university_id", "company_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniversityPartnership {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "university_id", nullable = false)
    private UUID universityId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartnershipStatus status = PartnershipStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_by", nullable = false)
    private PartnershipRequester requestedBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}