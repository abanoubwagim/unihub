package com.unihub.shared.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    @Override
    public void blacklist(String token, long ttlSeconds) {
        if (ttlSeconds > 0) {
            blacklist.put(token, Instant.now().getEpochSecond() + ttlSeconds);
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        Long expiry = blacklist.get(token);
        if (expiry == null) return false;
        if (Instant.now().getEpochSecond() > expiry) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }

    // Every hour automatic system removes expired tokens.
    @Scheduled(fixedRate = 3600000)
    public void cleanupExpired() {
        long now = Instant.now().getEpochSecond();
        blacklist.entrySet().removeIf(entry -> entry.getValue() < now);
    }
}