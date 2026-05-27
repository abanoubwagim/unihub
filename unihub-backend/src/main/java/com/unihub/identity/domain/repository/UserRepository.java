package com.unihub.identity.domain.repository;

import com.unihub.identity.domain.model.User;

import java.util.Optional;
import java.util.UUID;


public interface UserRepository {

    boolean existsByEmail(String email);

    User save(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findById(UUID id);

    void deleteById(UUID id);


}
