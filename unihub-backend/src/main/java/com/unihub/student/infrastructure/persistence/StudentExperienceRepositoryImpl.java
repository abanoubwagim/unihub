package com.unihub.student.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.unihub.student.domain.model.StudentExperience;
import com.unihub.student.domain.repository.StudentExperienceRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StudentExperienceRepositoryImpl implements StudentExperienceRepository {

    private final JpaStudentExperienceRepository jpa;

    @Override
    public Page<StudentExperience> findAllByStudent_UserId(UUID userId, Pageable pageable) {
        return jpa.findAllByStudent_UserId(userId, pageable);
    }

    @Override
    public Optional<StudentExperience> findByIdAndStudent_UserId(UUID id, UUID userId) {
        return jpa.findByIdAndStudent_UserId(id, userId);
    }

    @Override
    public Page<StudentExperience> findAllByStudent_Id(UUID studentId, Pageable pageable) {
        return jpa.findAllByStudent_Id(studentId, pageable);
    }

    @Override
    public Optional<StudentExperience> findByIdAndStudent_Id(UUID id, UUID studentId) {
        return jpa.findByIdAndStudent_Id(id, studentId);
    }

    @Override
    public Optional<StudentExperience> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public StudentExperience save(StudentExperience entity) {
        return jpa.save(entity);
    }

    @Override
    public void delete(StudentExperience entity) {
        jpa.delete(entity);
    }

}
