package com.unihub.shared.security.service;

import com.unihub.shared.security.JwtSubject;

import java.util.Optional;

public interface JwtService {
    String generateToken(JwtSubject subject);

    Optional<JwtSubject> parseAndValidate(String token);

    long getExpirationSeconds(String token);
}