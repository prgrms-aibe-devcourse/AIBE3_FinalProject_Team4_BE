package com.back.global.config.security.jwt;

import com.back.domain.user.refreshToken.entity.RefreshToken;
import com.back.domain.user.refreshToken.service.RefreshTokenService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;


@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final Rq rq;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        String accessToken = getTokenFromCookie("accessToken", req);
        String refreshToken = getTokenFromCookie("refreshToken", req);

        // 헤더에서 액세스 토큰 확인
        if(checkHeaderToken(req) != null) {
            accessToken = checkHeaderToken(req);
        }

        // 액세스 토큰이 유효한 경우 그대로 인증 처리
        if (accessToken != null && jwtProvider.validateToken(accessToken)) {
            SecurityUser securityUser = jwtProvider.parseUserFromAccessToken(accessToken);
            Authentication authentication = new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } //  액세스 토큰이 유효하지 않은 경우 리프레시 토큰 검사
        else if (refreshToken != null && jwtProvider.validateToken(refreshToken)) {
            logger.info("액세스 토큰 만료");

            Long userId = jwtProvider.getUserId(refreshToken);
            RefreshToken storedRefreshToken = refreshTokenService.getRefreshTokenByUserId(userId);

            // 1. 리프레시 토큰이 유효하지 않은 경우
            if (!storedRefreshToken.getToken().equals(refreshToken)) {
                SecurityContextHolder.clearContext();
                handleCustomAuthError(res, "리프레시 토큰이 유효하지 않습니다. 다시 로그인 해주세요.");
                return;
            }
            logger.info("리프레시 토큰 유효 확인 완료");

            // 2. 리프레시 토큰이 유효한 경우 새로운 액세스 토큰 발급
            String newAccessToken = jwtProvider.generateAccessToken(userId, "USER");
            rq.setCookie("accessToken", newAccessToken);
            SecurityUser securityUser = jwtProvider.parseUserFromAccessToken(newAccessToken);
            Authentication authentication = new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            logger.info("액세스 토큰 재발급 완료 / newAccessToken: " + newAccessToken);
        }

        chain.doFilter(req, res);
    }

    // 헤더에서 토큰 확인
    private String checkHeaderToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 쿠키에서 토큰 확인
    private String getTokenFromCookie(String cookieName, HttpServletRequest request){
        return Optional
                .ofNullable(request.getCookies())
                .flatMap(
                        cookies ->
                                Arrays.stream(request.getCookies())
                                        .filter(cookie -> cookieName.equals(cookie.getName()))
                                        .map(Cookie::getValue)
                                        .findFirst()
                )
                .orElse(null);
    }

    // 인증 오류 처리
    private void handleCustomAuthError(HttpServletResponse res, String message) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 응답 상태를 401로 설정
        res.setContentType("application/json;charset=UTF-8");   // 응답 콘텐츠 타입 설정

        RsData<Void> rsData = new RsData<>(
                "401-1",
                message
        );

        String jsonResponse = objectMapper.writeValueAsString(rsData);
        res.getWriter().write(jsonResponse);    // 응답 본문 작성
    }
}