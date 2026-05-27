package com.unihub.student.infrastructure.persistence.impl;

import java.util.Optional;
import java.util.UUID;

import com.unihub.student.infrastructure.persistence.jpa.JpaStudentCertificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.unihub.student.domain.model.StudentCertification;
import com.unihub.student.domain.repository.StudentCertificationRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StudentCertificationRepositoryImpl implements StudentCertificationRepository {
    private final JpaStudentCertificationRepository jpa;

    @Override
    public Optional<StudentCertification> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public StudentCertification save(StudentCertification entity) {
        return jpa.save(entity);
    }

    @Override
    public void delete(StudentCertification entity) {
        jpa.delete(entity);
    }

    @Override
    public Page<StudentCertification> findAllByStudent_UserId(UUID userId, Pageable pageable) {
        return jpa.findAllByStudent_UserId(userId, pageable);
    }

    @Override
    public Optional<StudentCertification> findByIdAndStudent_UserId(UUID id, UUID userId) {
        return jpa.findByIdAndStudent_UserId(id, userId);
    }

    @Override
    public Page<StudentCertification> findAllByStudent_Id(UUID studentId, Pageable pageable) {
        return jpa.findAllByStudent_Id(studentId, pageable);
    }

    @Override
    public Optional<StudentCertification> findByIdAndStudent_Id(UUID id, UUID studentId) {
        return jpa.findByIdAndStudent_Id(id, studentId);
    }

    @Override
    public void deleteAllByStudent_Id(UUID studentId) {
        jpa.deleteAllByStudent_Id(studentId);
    }
}