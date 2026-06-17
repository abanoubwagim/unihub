package com.unihub.shared.security.impl;

import com.unihub.shared.security.JwtSubject;
import com.unihub.shared.security.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    private final SecretKey secretKey;
    private final long jwtExpiration;

    public JwtServiceImpl(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long jwtExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        this.jwtExpiration = jwtExpiration;
    }

    @Override
    public String generateToken(JwtSubject subject) {
        return Jwts.builder()
                .subject(subject.id().toString())
                .claim("email", subject.email())
                .claim("role", subject.role())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(secretKey)
                .compact();
    }

    @Override
    public Optional<JwtSubject> parseAndValidate(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return Optional.of(new JwtSubject(
                    UUID.fromString(claims.getSubject()),
                    claims.get("email", String.class),
                    claims.get("role", String.class),
                    claims.getIssuedAt().toInstant().getEpochSecond()));
        } catch (ExpiredJwtException e) {
            log.debug("Token expired");
            return Optional.empty();
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public long getExpirationSeconds(String token) {
        return safeExtract(token, claims -> {
            long remaining = claims.getExpiration().getTime() - System.currentTimeMillis();
            return remaining > 0 ? remaining / 1000 : 0L;
        }, 0L);
    }
    
    private <T> T safeExtract(String token, Function<Claims, T> extractor, T defaultValue) {
        try {
            return extractor.apply(extractAllClaims(token));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}