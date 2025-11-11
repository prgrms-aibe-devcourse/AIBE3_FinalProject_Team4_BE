package com.back.domain.user.user.controller;

import com.back.domain.user.refreshToken.service.RefreshTokenService;
import com.back.domain.user.user.dto.UserDto;
import com.back.domain.user.user.dto.UserJoinRequestDto;
import com.back.domain.user.user.dto.UserLoginRequestDto;
import com.back.domain.user.user.dto.UserLoginResponseDto;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.service.UserService;
import com.back.global.config.security.SecurityUser;
import com.back.global.config.security.jwt.JwtTokenProvider;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class UserController {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final Rq rq;

    @PostMapping("/signup")
    public RsData<UserDto> join(@Valid @RequestBody UserJoinRequestDto dto) {
        User user = userService.join(dto);
        return new RsData<>(
            "201-1",
            "%s 님 가입을 환영합니다!".formatted(user.getUsername()),
            new UserDto(user)
        );
    }

    @PostMapping("/login")
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
    public RsData<Void> logout(@AuthenticationPrincipal SecurityUser securityUser) {
        userService.logout(securityUser.getId());
        rq.deleteCookie("accessToken");
        rq.deleteCookie("refreshToken");

        return new RsData<>(
                "200-1",
                "로그아웃 되었습니다."
        );
    }

    @GetMapping("/me")
    public RsData<UserDto> me(@AuthenticationPrincipal SecurityUser securityUser) {
        User user = userService.getUserById(securityUser.getId());
        return new RsData<>(
                "200-1",
                "사용자 정보입니다.",
                new UserDto(user)
        );
    }
}
