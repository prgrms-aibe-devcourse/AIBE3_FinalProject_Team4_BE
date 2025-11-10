package com.back.domain.user.user.controller;

import com.back.domain.user.user.dto.UserDto;
import com.back.domain.user.user.dto.UserJoinRequestDto;
import com.back.domain.user.user.dto.UserLoginRequestDto;
import com.back.domain.user.user.dto.UserLoginResponseDto;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.service.UserService;
import com.back.global.config.security.jwt.JwtTokenProvider;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class UserController {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
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

//        String accessToken = userService.generateAccessToken(user);
        String accessToken = jwtTokenProvider.generateToken(user.getId(), "");

        rq.setCookie("refreshToken", user.getRefreshToken());
        rq.setCookie("accessToken", accessToken);

        return new RsData<>(
                "200-1",
                "로그인 되었습니다.",
                new UserLoginResponseDto(new UserDto(user), user.getRefreshToken(), accessToken)
        );
    }

    @DeleteMapping("/logout")
    public RsData<Void> logout(HttpServletResponse response) {
        rq.deleteCookie("refreshToken");

        return new RsData<>(
                "200-1",
                "로그아웃 되었습니다."
        );
    }

    // Rq 클래스를 활용하는 방식
    @GetMapping("/me")
    public RsData<UserDto> me() {
        User user = rq.getActor();
        return new RsData<>(
                "200-1",
                "사용자 정보입니다.",
                new UserDto(user)
        );
    }

    @GetMapping("/me2")
    public RsData<UserDto> me2(Authentication auth) {
        User user = userService.getUserById((Long) auth.getPrincipal());
        return new RsData<>(
                "200-1",
                "사용자 정보입니다.",
                new UserDto(user)
        );
    }
}
