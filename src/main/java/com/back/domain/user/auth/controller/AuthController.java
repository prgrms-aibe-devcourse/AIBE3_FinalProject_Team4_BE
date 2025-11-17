package com.back.domain.user.auth.controller;

import com.back.domain.user.auth.dto.*;
import com.back.domain.user.refreshToken.service.RefreshTokenService;
import com.back.domain.user.user.dto.*;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.auth.service.AuthService;
import com.back.global.config.security.SecurityUser;
import com.back.global.config.security.jwt.JwtTokenProvider;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth API", description = "회원 및 인증/인가 토큰 API")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService userService;
    private final RefreshTokenService refreshTokenService;
    private final Rq rq;

    @PostMapping("/signup")
    @Operation(summary = "회원 가입")
    public RsData<UserDto> join(@Valid @RequestBody UserJoinRequestDto dto) {
        User user = userService.join(dto);
        return new RsData<>(
                "201-1",
                "%s 님 가입을 환영합니다!".formatted(user.getUsername()),
                new UserDto(user)
        );
    }

    @PostMapping("/complete-oauth2-join")
    @Operation(summary = "OAuth2 회원 가입 완료 및 로그인을 위한 추가 API")
    public RsData<UserDto> toCompleteJoinForOAuth2(@Valid @RequestBody OAuth2CompleteJoinRequestDto dto) {
        User user = userService.toCompleteJoinOAuth2User(dto);

        refreshTokenService.deleteRefreshTokenByUserId(user.getId());

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), "ROLE_USER");
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        refreshTokenService.saveRefreshToken(user.getId(), refreshToken);

        rq.setCookie("accessToken", accessToken);
        rq.setCookie("refreshToken", refreshToken);

        return new RsData<>(
                "201-1",
                "%s 님 가입을 환영합니다!".formatted(user.getUsername()),
                new UserDto(user)
        );
    }

    @PostMapping("/login")
    @Operation(summary = "로그인")
    public RsData<UserLoginResponseDto> login(@Valid @RequestBody UserLoginRequestDto dto) {
        User user = userService.login(dto);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), "ROLE_USER");
        rq.setCookie("accessToken", accessToken);

        refreshTokenService.deleteRefreshTokenByUserId(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        refreshTokenService.saveRefreshToken(user.getId(), refreshToken);
        rq.setCookie("refreshToken", refreshToken);

        return new RsData<>(
                "200-1",
                "로그인 되었습니다.",
                new UserLoginResponseDto(new UserDto(user), refreshToken, accessToken)
        );
    }

    @DeleteMapping("/logout")
    @Operation(summary = "로그아웃")
    public RsData<Void> logout(@AuthenticationPrincipal SecurityUser securityUser) {
        userService.logout(securityUser.getId());
        rq.deleteCookie("accessToken");
        rq.deleteCookie("refreshToken");

        return new RsData<>(
                "200-1",
                "로그아웃 되었습니다."
        );
    }

    @PostMapping("/password-reset")
    @Operation(summary = "비밀번호 재설정")
    public RsData<String> passwordReset(@RequestBody PasswordResetRequestDto dto) {
        userService.passwordReset(dto);
        return RsData.successOf("비밀번호가 재설정 되었습니다.");
    }

    @GetMapping("/me")
    @Operation(summary = "프로필 조회")
    public RsData<UserDto> me(@AuthenticationPrincipal SecurityUser securityUser) {
        User user = userService.getUserById(securityUser.getId());
        return new RsData<>(
                "200-1",
                "사용자 정보입니다.",
                new UserDto(user)
        );
    }
}