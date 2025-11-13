package com.back.global.config.security;

import com.back.domain.user.refreshToken.service.RefreshTokenService;
import com.back.global.config.security.jwt.JwtTokenProvider;
import com.back.global.rq.Rq;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final Rq rq;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException{
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        Long userId = securityUser.getId();

        System.out.println("securityUser : " + securityUser.getId() + ", " + securityUser.getEmail());

        // 소셜 가입 후 추가 정보가 없는 경우 프로필 완성 페이지로 리다이렉트
        String nickname = securityUser.getNickname();
        if (nickname == null) {
            redirectStrategy.sendRedirect(request, response, "/tmp-for-complete-join-of-oauth2-user");
//            response.sendRedirect("/tmp-for-complete-join-of-oauth2-user"); // todo 추후 프론트 페이지 URL로 변경
            return;
        }

        String accessToken = jwtTokenProvider.generateAccessToken(userId, "ROLE_USER");
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);
        refreshTokenService.saveRefreshToken(userId, refreshToken);

        rq.setCookie("accessToken", accessToken);
        rq.setCookie("refreshToken", refreshToken);

        response.sendRedirect("/"); // 로그인 후 리다이렉트할 URL 설정\
     }
}