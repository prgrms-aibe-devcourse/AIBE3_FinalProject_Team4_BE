package com.back.global.config.security;

import com.back.domain.user.refreshToken.service.RefreshTokenService;
import com.back.global.config.security.jwt.JwtTokenProvider;
import com.back.global.rq.Rq;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final Rq rq;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException{
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        Long userId = securityUser.getId();

        String accessToken = jwtTokenProvider.generateAccessToken(userId, "ROLE_USER");
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);
        refreshTokenService.saveRefreshToken(userId, refreshToken);

        rq.setCookie("accessToken", accessToken);
        rq.setCookie("refreshToken", refreshToken);

        response.sendRedirect("/"); // 로그인 후 리다이렉트할 URL 설정\
     }
}