package com.unihub.shared.security;

public interface TokenBlacklistService {

    void blacklist(String token, long ttlSeconds);
    boolean isBlacklisted(String token);
    void invalidateAllTokensBefore(String userId, long epochSeconds, long ttlSeconds);
    boolean isTokenIssuedBeforeInvalidation(String userId, long tokenIssuedAtEpoch);
}
