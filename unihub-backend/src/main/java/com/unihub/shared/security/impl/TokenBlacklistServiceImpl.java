package com.unihub.shared.security.impl;

import com.unihub.shared.security.service.TokenBlacklistService;
import com.unihub.shared.util.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String INVALIDATED_PREFIX = "user:pw_changed:";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void blacklist(String token, long ttlSeconds) {
        if (ttlSeconds > 0) {
            safeRedis(
                    () -> redisTemplate.opsForValue().set(toBlacklistKey(token), "1", ttlSeconds, TimeUnit.SECONDS),
                    "token not blacklisted"
            );
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        return safeRedis(
                () -> Boolean.TRUE.equals(redisTemplate.hasKey(toBlacklistKey(token))),
                false,
                "skipping blacklist check");
    }

    @Override
    public void invalidateAllTokensBefore(String userId, long epochSeconds, long ttlSeconds) {
        safeRedis(
                () -> redisTemplate.opsForValue().set(
                        INVALIDATED_PREFIX + userId,
                        String.valueOf(epochSeconds),
                        ttlSeconds,
                        TimeUnit.SECONDS),
                "token invalidation skipped"
        );
    }

    @Override
    public boolean isTokenIssuedBeforeInvalidation(String userId, long tokenIssuedAtEpoch) {
        return safeRedis(() -> {
            String val = redisTemplate.opsForValue().get(INVALIDATED_PREFIX + userId);
            if (val == null) return false;
            return tokenIssuedAtEpoch < Long.parseLong(val);
        }, false, "skipping invalidation check");
    }

    private <T> T safeRedis(Supplier<T> op, T fallback, String warnSuffix) {
        try {
            return op.get();
        } catch (Exception e) {
            log.warn("Redis unavailable — {}", warnSuffix);
            return fallback;
        }
    }

    private void safeRedis(Runnable op, String warnSuffix) {
        try {
            op.run();
        } catch (Exception e) {
            log.warn("Redis unavailable — {}", warnSuffix);
        }
    }

    private String toBlacklistKey(String token) {
        return BLACKLIST_PREFIX + TokenHashUtil.sha256(token);
    }
}