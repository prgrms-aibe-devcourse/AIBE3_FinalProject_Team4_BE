package com.back.domain.user.mail.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VerificationTokenService {
    private final RedisTemplate<String, String> redisTemplate;
    private final static long VERIFICATION_TOKEN_EXPIRATION = 10 * 60; // 10 minutes

    public String generateAndStoreToken(String email) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                "emailVerificationToken:" + email,
                token,
                VERIFICATION_TOKEN_EXPIRATION,
                TimeUnit.SECONDS
        );
        return token;
    }

    public boolean isValidToken(String email, String token) {
        String storedToken = redisTemplate.opsForValue().get("emailVerificationToken:" + email);
        return token.equals(storedToken);
    }

    public void deleteToken(String email) {
        redisTemplate.delete("emailVerificationToken:" + email);
    }

}
