package com.unihub.student.domain.repository;

import java.util.Optional;
import java.util.UUID;


import com.unihub.student.domain.model.StudentProfile;

public interface StudentProfileRepository {
    
    Optional<StudentProfile> findByUserId(UUID userId);
    Optional<StudentProfile> findById(UUID id);
    boolean existsByUserId(UUID userId);
    boolean existsById(UUID id);
    StudentProfile save(StudentProfile profile);

}
