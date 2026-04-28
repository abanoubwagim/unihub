package com.unihub.identity.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unihub.identity.domain.User;
import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

}
