package com.unihub.student.infrastructure.persistence.jpa;

import com.unihub.student.domain.model.StudentLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaStudentLinkRepository extends JpaRepository<StudentLink, UUID> {

    List<StudentLink> findAllByStudent_UserId(UUID userId);

    Optional<StudentLink> findByIdAndStudent_UserId(UUID id, UUID userId);

    @Modifying
    @Query("DELETE FROM StudentLink l WHERE l.student.id = :studentId")
    void deleteAllByStudent_Id(@Param("studentId") UUID studentId);
}