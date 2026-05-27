package com.unihub.student.infrastructure.persistence.impl;

import com.unihub.student.domain.model.StudentProfile;
import com.unihub.student.domain.repository.StudentProfileRepository;
import com.unihub.student.infrastructure.persistence.jpa.JpaStudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StudentProfileRepositoryImpl implements StudentProfileRepository {
    private final JpaStudentProfileRepository jpa;

    @Override
    public Optional<StudentProfile> findById(UUID Id) {
        return jpa.findById(Id);
    }

    @Override
    public Optional<StudentProfile> findByUserId(UUID userId) {
        return jpa.findByUserId(userId);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return jpa.existsByUserId(userId);
    }

    @Override
    public StudentProfile save(StudentProfile profile) {
        return jpa.save(profile);
    }

    @Override
    public void delete(StudentProfile profile) {
        jpa.delete(profile);
    }

    @Override
    public List<StudentProfile> findAllByUserIdIn(Set<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return jpa.findAllByUserIdIn(userIds);
    }

    @Override
    public Page<StudentProfile> findPageByUniversityId(UUID universityId, Pageable pageable) {
        return jpa.findAllByUniversityId(universityId, pageable);
    }


}