package com.back.domain.user.refreshToken.service;

import com.back.domain.user.refreshToken.entity.RefreshToken;
import com.back.domain.user.refreshToken.repository.RefreshTokenRepository;
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
        RefreshToken refreshToken = new RefreshToken(userId, token, new Date(System.currentTimeMillis() + refreshTokenExpireSeconds));
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken getRefreshTokenByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public void deleteRefreshTokenByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

}
