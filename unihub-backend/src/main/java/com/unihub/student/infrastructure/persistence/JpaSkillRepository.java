package com.unihub.student.infrastructure.persistence;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unihub.student.domain.model.Skill;

public interface JpaSkillRepository extends JpaRepository<Skill, UUID> {
    List<Skill> findAllByIdIn(Set<UUID> ids);
}