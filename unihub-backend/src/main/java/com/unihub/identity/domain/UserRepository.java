package com.unihub.identity.domain;

import java.util.Optional;


public interface UserRepository {

    boolean existsByEmail(String email);
    User save(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findById(java.util.UUID id);
    

    
}
