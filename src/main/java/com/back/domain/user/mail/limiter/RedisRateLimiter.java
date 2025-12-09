package com.back.domain.user.mail.limiter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisRateLimiter {
    private final RedisTemplate<String, String> redisTemplate;

    public boolean allow(String key, long limit, Duration window) {
        Long count = redisTemplate.opsForValue().increment(key);

        // 최초 1회면 TTL 세팅
        if (count != null && count == 1L) {
            redisTemplate.expire(key, window.toSeconds(), TimeUnit.SECONDS);
        }
        return count != null && count <= limit;
    }
}
