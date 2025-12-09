package com.back.domain.user.mail.limiter;

import com.back.global.exception.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class MailRateLimitService {
    private final RedisRateLimiter limiter;

    public void checkSendCodeOrThrow(String ip, String email) {
        String normEmail = normalizeEmail(email);

        if (!limiter.allow(key("mail", "sendCode", "ip", ip, "1m"), 10, Duration.ofMinutes(1))) {
            throw new AuthException("400-5", "요청 한도를 초과했습니다. 1분 후 다시 시도해주세요.");
        }
        if (!limiter.allow(key("mail", "sendCode", "ip", ip, "10m"), 30, Duration.ofMinutes(10))) {
            throw new AuthException("400-9", "요청 한도를 초과했습니다. 10분 후 다시 시도해주세요.");
        }
        if (!limiter.allow(key("mail", "sendCode", "ip", ip, "1d"), 300, Duration.ofDays(1))) {
            throw new AuthException("400-6", "요청 한도를 초과했습니다. 1일 후 다시 시도해주세요.");
        }

        if (!limiter.allow(key("mail", "sendCode", "email", normEmail, "1m"), 2, Duration.ofMinutes(1))) {
            throw new AuthException("400-4", "요청 한도를 초과했습니다. 1분 후 다시 시도해주세요.");
        }
        if (!limiter.allow(key("mail", "sendCode", "email", normEmail, "10m"), 4, Duration.ofMinutes(10))) {
            throw new AuthException("400-7", "요청 한도를 초과했습니다. 10분 후 다시 시도해주세요.");
        }
        if (!limiter.allow(key("mail", "sendCode", "email", normEmail, "1d"), 10, Duration.ofDays(1))) {
            throw new AuthException("400-8", "요청 한도를 초과했습니다. 1일 후 다시 시도해주세요.");
        }
    }

    private String key(String domain, String action, String subject, String value, String window) {
        return String.join(":", domain, action, subject, value, window);
    }

    private String normalizeEmail(String email) {
        return email == null ? "null" : email.trim().toLowerCase();
    }
}