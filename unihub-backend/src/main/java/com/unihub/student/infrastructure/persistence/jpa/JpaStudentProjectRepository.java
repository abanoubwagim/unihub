package com.unihub.student.infrastructure.persistence.jpa;

import com.unihub.student.domain.model.StudentProject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaStudentProjectRepository extends JpaRepository<StudentProject, UUID> {

    @EntityGraph(attributePaths = {"skills"})
    Page<StudentProject> findAllByStudent_UserId(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"skills"})
    Optional<StudentProject> findByIdAndStudent_UserId(UUID id, UUID userId);

    @EntityGraph(attributePaths = {"skills"})
    Page<StudentProject> findAllByStudent_Id(UUID studentId, Pageable pageable);

    @EntityGraph(attributePaths = {"skills"})
    Optional<StudentProject> findByIdAndStudent_Id(UUID id, UUID studentId);

    @Override
    @EntityGraph(attributePaths = {"skills"})
    Optional<StudentProject> findById(UUID id);

    @Modifying
    @Query("DELETE FROM StudentProject p WHERE p.student.id = :studentId")
    void deleteAllByStudent_Id(@Param("studentId") UUID studentId);
}