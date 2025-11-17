package com.back.domain.user.user.controller;

import com.back.domain.user.user.dto.UpdateProfileRequestDto;
import com.back.domain.user.user.dto.UserDto;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.service.UserService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
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
    public RsData<List<UserDto>> getUsers() {
        List<UserDto> userDtos =  userService.getAllUsers();
        return RsData.of("200", "유저 목록 조회 성공", userDtos);
    }

    @GetMapping("/{id}")
    public RsData<UserDto> getUserById(@PathVariable Long id) {
        UserDto userDto = userService.getUserById(id);
        return RsData.of("200", "유저 조회 성공", userDto);
    }

    @GetMapping("/me")
    public RsData<UserDto> getMyProfile(@AuthenticationPrincipal SecurityUser user) {
        UserDto userDto = userService.getUserById(user.getId());
        return RsData.of("200", "내 프로필 조회 성공", userDto);
    }

    @PutMapping("/update")
    public RsData<UserDto> updateMyProfile(@AuthenticationPrincipal SecurityUser user, @Valid @RequestBody UpdateProfileRequestDto dto) {
        UserDto userDto = userService.updateProfile(user.getId(), dto);
        return RsData.of("200", "프로필 수정 성공", userDto);
    }

    @GetMapping("/check-nickname")
    public RsData<Void> checkNicknameAvailable(@RequestParam String nickname) {
        boolean result = userService.isAvailableNickname(nickname);
        if(result) {
            return RsData.of("200", "사용 가능한 닉네임입니다.", null);
        } else {
            return RsData.of("409", "이미 사용 중인 닉네임입니다.", null);
        }
    }

}
