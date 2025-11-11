package com.back.domain.user.refreshToken.service;

import com.back.domain.user.refreshToken.entity.RefreshToken;
import com.back.domain.user.refreshToken.repository.RefreshTokenRepository;
import com.back.global.exception.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    @Value("${jwt.refresh-exp}")
    private long refreshTokenExpireSeconds;

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void saveRefreshToken(Long userId, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(token)
                .expiration(refreshTokenExpireSeconds)
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken getRefreshTokenByUserId(Long userId) {
        return refreshTokenRepository.findById(userId)
                .orElseThrow(() -> new AuthException("401-2", "리프레시 토큰이 존재하지 않습니다."));
    }

    @Transactional
    public void deleteRefreshTokenByUserId(Long userId) {
        refreshTokenRepository.deleteById(userId);
    }

}
