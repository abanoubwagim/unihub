package com.unihub.university.infrastructure.persistence.impl;

import com.unihub.university.domain.model.Major;
import com.unihub.university.domain.repository.MajorRepository;
import com.unihub.university.infrastructure.persistence.jpa.JpaMajorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MajorRepositoryImpl implements MajorRepository {

    private final JpaMajorRepository jpa;

    @Override
    public List<Major> findAll() {
        return jpa.findAll();
    }

    @Override
    public Optional<Major> findById(UUID id) {
        return jpa.findById(id);
    }
}