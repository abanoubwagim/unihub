package com.unihub.student.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.unihub.student.domain.model.StudentProject;
import com.unihub.student.domain.repository.StudentProjectRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StudentProjectRepositoryImpl implements StudentProjectRepository {
    private final JpaStudentProjectRepository jpa;

    @Override
    public Optional<StudentProject> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public StudentProject save(StudentProject entity) {
        return jpa.save(entity);
    }

    @Override
    public void delete(StudentProject entity) {
        jpa.delete(entity);
    }

    @Override
    public Page<StudentProject> findAllByStudent_UserId(UUID userId, Pageable pageable) {
        return jpa.findAllByStudent_UserId(userId, pageable);
    }

    @Override
    public Optional<StudentProject> findByIdAndStudent_UserId(UUID id, UUID userId) {
        return jpa.findByIdAndStudent_UserId(id, userId);
    }

    @Override
    public Page<StudentProject> findAllByStudent_Id(UUID studentId, Pageable pageable) {
        return jpa.findAllByStudent_Id(studentId, pageable);
    }

    @Override
    public Optional<StudentProject> findByIdAndStudent_Id(UUID id, UUID studentId) {
        return jpa.findByIdAndStudent_Id(id, studentId);
    }

    @Override
    public void deleteAllByStudent_Id(UUID studentId) {
        jpa.deleteAllByStudent_Id(studentId);
    }
}
