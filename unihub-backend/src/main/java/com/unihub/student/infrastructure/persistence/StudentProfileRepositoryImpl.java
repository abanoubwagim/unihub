package com.unihub.student.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.unihub.student.domain.model.StudentProfile;
import com.unihub.student.domain.repository.StudentProfileRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StudentProfileRepositoryImpl implements StudentProfileRepository {
    private final JpaStudentProfileRepository jpa;

    @Override
    public Optional<StudentProfile> findByUserId(UUID userId) {
        return jpa.findByUserId(userId);
    }

    @Override
    public Optional<StudentProfile> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return jpa.existsByUserId(userId);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpa.existsById(id);
    }

    @Override
    public StudentProfile save(StudentProfile profile) {
        return jpa.save(profile);
    }
}
