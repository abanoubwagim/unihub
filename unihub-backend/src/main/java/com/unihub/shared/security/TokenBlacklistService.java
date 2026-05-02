package com.unihub.shared.security;

public interface TokenBlacklistService {

    void blacklist(String token, long ttlSeconds);
    boolean isBlacklisted(String token);
}
