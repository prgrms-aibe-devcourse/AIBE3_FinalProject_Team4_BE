package com.back.domain.user.refreshToken.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.Date;

@Getter
@NoArgsConstructor
@RedisHash("refreshToken")
public class RefreshToken {

    @Id
    private Long userId;

    private String token;

    @TimeToLive
    private Long expiration;

    @Builder
    public RefreshToken(Long userId, String token, Long expiration) {
        this.userId = userId;
        this.token = token;
        this.expiration = expiration / 1000; // Redis TTL은 초 단위이므로 밀리초를 초로 변환
    }
}
