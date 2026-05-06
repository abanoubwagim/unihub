package com.unihub.student.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.unihub.student.domain.model.GraduationCertificate;
import com.unihub.student.domain.repository.GraduationCertificateRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class GraduationCertificateRepositoryImpl implements GraduationCertificateRepository {
    private final JpaGraduationCertificateRepository jpa;

    @Override
    public Optional<GraduationCertificate> findTopByStudentIdOrderByAttemptNumberDesc(UUID studentId) {
        return jpa.findTopByStudentIdOrderByAttemptNumberDesc(studentId);
    }

    @Override
    public int countByStudentId(UUID studentId) {
        return jpa.countByStudentId(studentId);
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
}
