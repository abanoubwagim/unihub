package com.unihub.shared.security;

public interface JwtService {
    String generateToken(JwtSubject subject);

    boolean isTokenValid(String token);
   
   JwtSubject extractSubject(String token);

   long getExpirationSeconds(String token);
}