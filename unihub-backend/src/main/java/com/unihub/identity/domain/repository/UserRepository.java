package com.unihub.identity.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.unihub.identity.domain.model.User;


public interface UserRepository {

    boolean existsByEmail(String email);
    User save(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findById(java.util.UUID id);
    void deleteById(UUID id);
    

    
}
