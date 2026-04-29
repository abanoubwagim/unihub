package com.unihub.identity.domain;

import java.util.Optional;
import java.util.UUID;


public interface UserRepository {

    boolean existsByEmail(String email);
    User save(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findById(java.util.UUID id);
    void deleteById(UUID id);
    

    
}
