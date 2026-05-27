package com.unihub.student.infrastructure.persistence.impl;

import com.unihub.student.domain.model.StudentLink;
import com.unihub.student.domain.repository.StudentLinkRepository;
import com.unihub.student.infrastructure.persistence.jpa.JpaStudentLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StudentLinkRepositoryImpl implements StudentLinkRepository {

    private final JpaStudentLinkRepository jpa;

    @Override
    public Optional<StudentLink> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public StudentLink save(StudentLink entity) {
        return jpa.save(entity);
    }

    @Override
    public void delete(StudentLink entity) {
        jpa.delete(entity);
    }

    @Override
    public List<StudentLink> findAllByStudent_UserId(UUID userId) {
        return jpa.findAllByStudent_UserId(userId);
    }

    @Override
    public Optional<StudentLink> findByIdAndStudent_UserId(UUID id, UUID userId) {
        return jpa.findByIdAndStudent_UserId(id, userId);
    }

    @Override
    public void deleteAllByStudent_Id(UUID studentId) {
        jpa.deleteAllByStudent_Id(studentId);
    }
}