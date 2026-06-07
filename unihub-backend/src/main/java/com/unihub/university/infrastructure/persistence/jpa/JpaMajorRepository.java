package com.unihub.university.infrastructure.persistence.jpa;

import com.unihub.university.domain.model.Major;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaMajorRepository extends JpaRepository<Major, UUID> {
}
