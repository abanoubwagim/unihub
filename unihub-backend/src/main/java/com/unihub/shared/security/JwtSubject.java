package com.unihub.shared.security;

import java.util.UUID;

public record JwtSubject(
        UUID id,
        String email,
        String role,
        long issuedAtEpochSeconds
) {

    public JwtSubject(UUID id, String email, String role) {
        this(id, email, role, 0L);
    }
}
