package com.back.global.config.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;


@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        // 인증 인가 필요 없는 요청
        if(req.getRequestURI().startsWith("/swagger-ui") ||
                req.getRequestURI().startsWith("/v3/api-docs") ||
                req.getRequestURI().startsWith("/api/v1/auth/signup") ||
                req.getRequestURI().startsWith("/api/v1/auth/login") ||
                req.getRequestURI().startsWith("/api/v1/auth/logout")
        ) {
            chain.doFilter(req, res);
            return;
        }


        String accessToken = getTokenFromRequest(req, res);
        if (accessToken != null) {
            if (jwtProvider.validateToken(accessToken)) {
                Long userId = jwtProvider.getUserId(accessToken);
                JwtAuthentication authentication = new JwtAuthentication(userId);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                SecurityContextHolder.clearContext();
                handleCustomAuthError(res, "유효하지 않은 토큰입니다.");
                return;
            }
        } else {
            SecurityContextHolder.clearContext();
            handleCustomAuthError(res, "토큰 정보가 없습니다.");
            return;
        }

        chain.doFilter(req, res);
    }

    private String getTokenFromRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String accessToken;
        // 헤더에서 토큰 가져오기
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null) {
            if (!bearerToken.startsWith("Bearer ")) {
                handleCustomAuthError(response, "잘못된 토큰 형식입니다.");
            }
            accessToken = bearerToken.substring(7);
        } else {    // 쿠키에서 토큰 가져오기
            accessToken = Optional
                    .ofNullable(request.getCookies())
                    .flatMap(
                            cookies ->
                                    Arrays.stream(request.getCookies())
                                            .filter(cookie -> cookie.getName().equals("accessToken"))
                                            .map(Cookie::getValue)
                                            .findFirst()
                    )
                    .orElse("");
        }

        if (!accessToken.isEmpty()) {
            return accessToken;
        } else {
            return null;
        }
    }

    private void handleCustomAuthError(HttpServletResponse res, String message) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 응답 상태를 401로 설정
        res.setContentType("application/json;charset=UTF-8");   // 응답 콘텐츠 타입 설정
        String jsonResponse = String.format("{\"msg\": \"%s\"}", message);  // JSON 형식의 응답 본문 생성
        res.getWriter().write(jsonResponse);    // 응답 본문 작성
    }
}