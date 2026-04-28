package com.unihub.shared.security;

import java.util.UUID;
public record JwtSubject(
    UUID id,
    String email,
    String role
) {

}
