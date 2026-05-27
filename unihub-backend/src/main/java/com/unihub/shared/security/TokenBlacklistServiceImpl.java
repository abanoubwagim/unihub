package com.unihub.shared.security;

import com.unihub.shared.util.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String INVALIDATED_PREFIX = "user:pw_changed:";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void blacklist(String token, long ttlSeconds) {
        try {
            if (ttlSeconds > 0) {
                redisTemplate.opsForValue()
                        .set(toBlacklistKey(token), "1", ttlSeconds, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.warn("Redis unavailable — token not blacklisted");
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(toBlacklistKey(token)));
        } catch (Exception e) {
            log.warn("Redis unavailable — skipping blacklist check");
            return false;
        }
    }

    @Override
    public void invalidateAllTokensBefore(String userId, long epochSeconds, long ttlSeconds) {
        try {
            redisTemplate.opsForValue()
                    .set(INVALIDATED_PREFIX + userId,
                            String.valueOf(epochSeconds),
                            ttlSeconds,
                            TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis unavailable — token invalidation skipped");
        }
    }

    @Override
    public boolean isTokenIssuedBeforeInvalidation(String userId, long tokenIssuedAtEpoch) {
        try {
            String val = redisTemplate.opsForValue().get(INVALIDATED_PREFIX + userId);
            if (val == null) return false;
            long invalidatedAt = Long.parseLong(val);
            return tokenIssuedAtEpoch < invalidatedAt;
        } catch (Exception e) {
            log.warn("Redis unavailable — skipping invalidation check");
            return false;
        }
    }

    private String toBlacklistKey(String token) {
        return BLACKLIST_PREFIX + TokenHashUtil.sha256(token);
    }
}