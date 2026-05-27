package com.unihub.student.domain.model;

import com.unihub.student.domain.enums.GraduationCertificateStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "graduation_certificates")
@Getter
@Setter
@NoArgsConstructor
public class GraduationCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "university_id", nullable = false)
    private UUID universityId;

    @Column(name = "file_url")
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GraduationCertificateStatus status = GraduationCertificateStatus.PENDING;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber = 0;

    @CreationTimestamp
    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;
}
