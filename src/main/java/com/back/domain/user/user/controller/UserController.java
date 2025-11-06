package com.back.domain.user.user.controller;

import com.back.domain.user.user.dto.UserDto;
import com.back.domain.user.user.dto.UserJoinRequestDto;
import com.back.domain.user.user.dto.UserLoginRequestDto;
import com.back.domain.user.user.dto.UserLoginResponseDto;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.service.UserService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class UserController {
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

        rq.setCookie("apiKey", user.getApiKey());

        return new RsData<>(
                "200-1",
                "로그인 되었습니다.",
                new UserLoginResponseDto(new UserDto(user), user.getApiKey())
        );
    }

    @DeleteMapping("/logout")
    public RsData<Void> logout(HttpServletResponse response) {
        rq.deleteCookie("apiKey");

        return new RsData<>(
                "200-1",
                "로그아웃 되었습니다."
        );
    }


//    // 헤더에서 직접 Authorization 값을 받아오는 방식
//    @GetMapping("/me")
//    public RsData<UserDto> me(@RequestHeader("Authorization") String authorization) {
//        String apiKey = authorization.replace("Bearer ", "");
//        User user = userService.getUserByApiKey(apiKey);
//        return new RsData<>(
//                "200-1",
//                "사용자 정보입니다.",
//                new UserDto(user)
//        );
//    }

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
}
