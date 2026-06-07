package com.unihub.student.domain.repository;

import com.unihub.student.domain.model.StudentProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface StudentProfileRepository {

    Optional<StudentProfile> findById(UUID Id);

    Optional<StudentProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    StudentProfile save(StudentProfile profile);

    void delete(StudentProfile profile);

    List<StudentProfile> findAllByIdIn(Set<UUID> userIds);

    Page<StudentProfile> findPageByUniversityId(UUID universityId, Pageable pageable);

}
