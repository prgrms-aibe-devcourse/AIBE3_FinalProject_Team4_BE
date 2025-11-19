package com.back.global.config.security;

import com.back.domain.user.refreshToken.service.RefreshTokenService;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException{
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        Long userId = securityUser.getId();
        User user = userRepository.findById(userId).orElseThrow();

        // 소셜 가입은 프로필 완성 페이지로 리다이렉트, 이후 컨트롤러에서 별도 토큰 발급
        String nickname = user.getNickname();
        if (nickname == null) {
            String targetUrl = "/tmp-for-complete-join-of-oauth2-user";     // todo 추후 프론트 Next.js 페이지 URL로 변경
            String token = jwtTokenProvider.generateTemporaryToken(userId);
            System.out.println("임시 토큰이 발급되었습니다. : " + token);
            String redirectUrlWithToken = targetUrl + "?token=" + token;

            redirectStrategy.sendRedirect(request, response, redirectUrlWithToken);
            return;
        }

        // 가입되어있는 소셜 사용자
        // 기존 리프레시 토큰 삭제 후
        refreshTokenService.deleteRefreshTokenByUserId(userId);
        // 새로운 토큰 발급
        String accessToken = jwtTokenProvider.generateAccessToken(userId, securityUser.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);
        refreshTokenService.saveRefreshToken(userId, refreshToken);

        rq.setCookie("accessToken", accessToken);
        rq.setCookie("refreshToken", refreshToken);

        response.sendRedirect("/"); // 로그인 후 리다이렉트할 URL 설정\
     }
}