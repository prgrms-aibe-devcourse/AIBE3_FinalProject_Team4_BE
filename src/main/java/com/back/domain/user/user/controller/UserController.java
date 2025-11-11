package com.back.domain.user.user.controller;

import com.back.domain.user.refreshToken.service.RefreshTokenService;
import com.back.domain.user.user.dto.*;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.service.UserService;
import com.back.global.config.security.SecurityUser;
import com.back.global.config.security.jwt.JwtTokenProvider;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth API", description = "회원 및 인증/인가 토큰 API")
public class UserController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final Rq rq;

    @PostMapping("/signup")
    @Operation(
            summary = "회원 가입",
            description = "신규 사용자를 회원가입시킵니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "회원 가입 성공",
                            content = @Content(schema = @Schema(implementation = UserDto.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                            content = @Content(schema = @Schema(implementation = RsData.class))),
                    @ApiResponse(responseCode = "409", description = "이미 존재하는 사용자",
                            content = @Content(schema = @Schema(implementation = RsData.class)))
            }
    )
    public RsData<UserDto> join(@Valid @RequestBody UserJoinRequestDto dto) {
        User user = userService.join(dto);
        return new RsData<>(
                "201-1",
                "%s 님 가입을 환영합니다!".formatted(user.getUsername()),
                new UserDto(user)
        );
    }

    @PostMapping("/login")
    @Operation(
            summary = "로그인",
            description = "사용자 인증 후 AccessToken과 RefreshToken을 발급합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공",
                            content = @Content(schema = @Schema(implementation = UserLoginResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "아이디 또는 비밀번호 오류",
                            content = @Content(schema = @Schema(implementation = RsData.class))),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = RsData.class)))
            }
    )
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
    @Operation(
            summary = "로그아웃",
            description = "사용자 쿠키에서 토큰을 삭제하고, 서버의 RefreshToken을 제거합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그아웃 성공",
                            content = @Content(schema = @Schema(implementation = RsData.class))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                            content = @Content(schema = @Schema(implementation = RsData.class))),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = RsData.class)))
            }
    )
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
    @Operation(
            summary = "프로필 조회",
            description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = UserDto.class))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                            content = @Content(schema = @Schema(implementation = RsData.class))),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = RsData.class)))
            }
    )
    public RsData<UserDto> me(@AuthenticationPrincipal SecurityUser securityUser) {
        User user = userService.getUserById(securityUser.getId());
        return new RsData<>(
                "200-1",
                "사용자 정보입니다.",
                new UserDto(user)
        );
    }
}
