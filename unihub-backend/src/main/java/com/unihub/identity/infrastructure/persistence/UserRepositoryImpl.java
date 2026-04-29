package com.unihub.identity.infrastructure.persistence;

import com.unihub.identity.domain.model.User;
import com.unihub.identity.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpa;

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email);
    }

    @Override
    public User save(User user) {
        return jpa.save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }
}