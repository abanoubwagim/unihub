package com.unihub.shared.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.unihub.shared.util.TokenHashUtil;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final String PREFIX = "blacklist:";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void blacklist(String token, long ttlSeconds) {

        if (ttlSeconds > 0) {
            redisTemplate.opsForValue()
                    .set(toKey(token), "1", ttlSeconds, TimeUnit.SECONDS);
        }

    }

    @Override
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(toKey(token)));
    }

    private String toKey(String token) {
        return PREFIX + TokenHashUtil.sha256(token);
    }
}