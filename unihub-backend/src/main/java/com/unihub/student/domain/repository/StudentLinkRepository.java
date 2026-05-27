package com.unihub.student.domain.repository;

import com.unihub.student.domain.model.StudentLink;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentLinkRepository {

    Optional<StudentLink> findById(UUID id);

    StudentLink save(StudentLink entity);

    void delete(StudentLink entity);

    // For authenticated student (by userId)
    List<StudentLink> findAllByStudent_UserId(UUID userId);

    Optional<StudentLink> findByIdAndStudent_UserId(UUID id, UUID userId);

    // Bulk delete — used during account deletion cleanup
    void deleteAllByStudent_Id(UUID studentId);
}