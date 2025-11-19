package com.back.global.config;

import com.back.domain.user.refreshToken.service.RefreshTokenService;
import com.back.global.config.security.CustomOAuth2UserService;
import com.back.global.config.security.OAuth2LoginSuccessHandler;
import com.back.global.config.security.jwt.JwtAuthenticationFilter;
import com.back.global.config.security.jwt.JwtTokenProvider;
import com.back.global.rq.Rq;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    // 무조건 허용 URL 패턴
    private static final String[] ALWAYS_PERMIT = {
            "/",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/v1/blogs/**"          // TODO: 추후 인증 필요
    };

    // 공개된 API URL 패턴 [GET 요청에 한함]
    private static final String[] PUBLIC_GET_API = {
            "/api/v1/users",
            "/api/v1/users/{id:\\d+}",          // * 이나 {id}로 하면 me/my 까지 포함되어 버림
            "/api/v1/users/check-nickname",

            "/api/v1/follow/followers/{id:\\d+}",
            "/api/v1/follow/followings/{id:\\d+}",
            "/api/v1/follow/counts/{id:\\d+}",

            "/api/v1/shorlog/{id}",
            "/api/v1/shorlog/feed",
            "/api/v1/shorlog/search"
    };

    // 공개된 인증 API URL 패턴 [메서드 무관]
    private static final String[] AUTH_WHITELIST = {
            "/api/v1/auth/signup",
            "/api/v1/auth/login",
            "/api/v1/auth/password-reset",
            "/api/v1/auth/complete-oauth2-join",
            "/api/v1/auth/send-code",
            "/api/v1/auth/verify-code",
            "/tmp-for-complete-join-of-oauth2-user"    // todo 추후 프론트 페이지 개발 후 제거
    };

    private final JwtTokenProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final Rq rq;
    private final ObjectMapper objectMapper;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider, refreshTokenService, rq, objectMapper);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomOAuth2UserService customOAuth2UserService) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authenticationException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"resultCode\":\"401-1\",\"message\":\"인증이 필요합니다.\"}");
                        }))
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(ALWAYS_PERMIT).permitAll()
                        .requestMatchers(AUTH_WHITELIST).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_API) .permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))      // DB 연동 및 회원가입 처리
                        .successHandler(oAuth2LoginSuccessHandler)          // 로그인 성공 후 JWT 발급, 리다이렉트 처리
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}