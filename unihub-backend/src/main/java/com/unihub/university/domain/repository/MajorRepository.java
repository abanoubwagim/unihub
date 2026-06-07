package com.unihub.university.domain.repository;

import com.unihub.university.domain.model.Major;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MajorRepository {

    List<Major> findAll();

    Optional<Major> findById(UUID id);
}
