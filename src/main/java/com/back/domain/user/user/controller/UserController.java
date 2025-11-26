package com.back.domain.user.user.controller;

import com.back.domain.user.user.dto.*;
import com.back.domain.user.user.service.UserService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "유저 API")
public class UserController {
    private final UserService userService;

    @GetMapping()
    @Operation(summary = "전체 유저 목록 조회")
    public RsData<List<UserListResponseDto>> getUsers() {
        List<UserListResponseDto> userDtos =  userService.getAllUsers();
        return RsData.of("200", "유저 목록 조회 성공", userDtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "특정 유저 조회")
    public RsData<ProfileResponseDto> getUserById(@PathVariable Long id) {
        ProfileResponseDto userProfileResponseDto = userService.getUserById(id);
        return RsData.of("200", "유저 조회 성공", userProfileResponseDto);
    }

    @GetMapping("/me")
    @Operation(summary = "내 프로필 조회")
    public RsData<MyProfileResponseDto> getMyProfile(@AuthenticationPrincipal SecurityUser user) {
        MyProfileResponseDto myProfileResponseDto = userService.getMyUser(user.getId());
        return RsData.of("200", "내 프로필 조회 성공", myProfileResponseDto);
    }

    @PutMapping("/update")
    @Operation(summary = "내 프로필 수정")
    public RsData<UserDto> updateMyProfile(@AuthenticationPrincipal SecurityUser user, @Valid @RequestBody UpdateProfileRequestDto dto) {
        UserDto userDto = userService.updateProfile(user.getId(), dto);
        return RsData.of("200", "프로필 수정 성공", userDto);
    }

    @GetMapping("/check-nickname")
    @Operation(summary = "닉네임 중복 확인")
    public RsData<Void> checkNicknameAvailable(@RequestParam String nickname) {
        boolean result = userService.isAvailableNickname(nickname);
        if(result) {
            return RsData.of("200", "사용 가능한 닉네임입니다.", null);
        } else {
            return RsData.of("409", "이미 사용 중인 닉네임입니다.", null);
        }
    }

    @GetMapping("/search")
    @Operation(summary = "유저 검색")
    public RsData<List<UserListResponseDto>> searchUser(@Valid @RequestParam String keyword) {
        List<UserListResponseDto> users = userService.searchUserByKeyword(keyword);
        return RsData.of("200", "유저 검색 성공", users);
    }

}
