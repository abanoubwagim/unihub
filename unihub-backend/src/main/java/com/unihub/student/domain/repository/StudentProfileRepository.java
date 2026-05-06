package com.unihub.student.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;


import com.unihub.student.domain.model.StudentProfile;

public interface StudentProfileRepository {
    
    Optional<StudentProfile> findByUserId(UUID userId);
    Optional<StudentProfile> findById(UUID id);
    boolean existsByUserId(UUID userId);
    boolean existsById(UUID id);
    StudentProfile save(StudentProfile profile);
    void delete(StudentProfile profile);
    List<StudentProfile> findAllByUserIdIn(Set<UUID> userIds);

}
