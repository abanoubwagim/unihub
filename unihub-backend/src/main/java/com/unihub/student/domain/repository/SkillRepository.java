package com.unihub.student.domain.repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.unihub.student.domain.model.Skill;

public interface SkillRepository {
 
    List<Skill> findAllByIdIn(Set<UUID> ids);
}
