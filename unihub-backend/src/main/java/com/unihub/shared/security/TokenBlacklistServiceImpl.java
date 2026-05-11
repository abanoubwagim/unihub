package com.unihub.shared.security;

import com.unihub.shared.util.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String INVALIDATED_PREFIX = "user:pw_changed:";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void blacklist(String token, long ttlSeconds) {
        if (ttlSeconds > 0) {
            redisTemplate.opsForValue()
                    .set(toBlacklistKey(token), "1", ttlSeconds, TimeUnit.SECONDS);
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(toBlacklistKey(token)));
    }

    @Override
    public void invalidateAllTokensBefore(String userId, long epochSeconds, long ttlSeconds) {

        redisTemplate.opsForValue()
                .set(INVALIDATED_PREFIX + userId,
                        String.valueOf(epochSeconds),
                        ttlSeconds,
                        TimeUnit.SECONDS);
    }

    @Override
    public boolean isTokenIssuedBeforeInvalidation(String userId, long tokenIssuedAtEpoch) {
        String val = redisTemplate.opsForValue().get(INVALIDATED_PREFIX + userId);
        if (val == null) {
            return false;
        }
        try {
            long invalidatedAt = Long.parseLong(val);
            return tokenIssuedAtEpoch < invalidatedAt;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String toBlacklistKey(String token) {
        return BLACKLIST_PREFIX + TokenHashUtil.sha256(token);
    }
}