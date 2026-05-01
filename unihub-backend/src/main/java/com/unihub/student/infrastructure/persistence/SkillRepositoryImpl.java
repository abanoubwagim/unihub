package com.unihub.student.infrastructure.persistence;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.unihub.student.domain.model.Skill;
import com.unihub.student.domain.repository.SkillRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SkillRepositoryImpl implements SkillRepository {
    private final JpaSkillRepository jpa;

    @Override
    public List<Skill> findAllByIdIn(Set<UUID> ids) {
        return jpa.findAllByIdIn(ids);
    }
}