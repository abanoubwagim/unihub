package com.unihub.student.infrastructure.persistence.impl;

import com.unihub.student.domain.enums.GraduationCertificateStatus;
import com.unihub.student.domain.model.GraduationCertificate;
import com.unihub.student.domain.repository.GraduationCertificateRepository;
import com.unihub.student.infrastructure.persistence.jpa.JpaGraduationCertificateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class GraduationCertificateRepositoryImpl implements GraduationCertificateRepository {
    private final JpaGraduationCertificateRepository jpa;

    @Override
    public Optional<GraduationCertificate> findTopByStudentIdOrderByAttemptNumberDesc(UUID studentId) {
        return jpa.findTopByStudentIdOrderByAttemptNumberDesc(studentId);
    }

    @Override
    public int countByStudentIdAndStatus(UUID studentId, GraduationCertificateStatus status) {
        return jpa.countByStudentIdAndStatus(studentId, status);
    }

    @Override
    public Optional<GraduationCertificate> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public GraduationCertificate save(GraduationCertificate cert) {
        return jpa.save(cert);
    }

    @Override
    public void deleteAllByStudentId(UUID studentId) {
        jpa.deleteAllByStudentId(studentId);
    }

    @Override
    public Page<GraduationCertificate> findAllByUniversityIdAndStatus(UUID universityId, GraduationCertificateStatus status, Pageable pageable) {
        return jpa.findAllByUniversityIdAndStatus(universityId, status, pageable);
    }
}
