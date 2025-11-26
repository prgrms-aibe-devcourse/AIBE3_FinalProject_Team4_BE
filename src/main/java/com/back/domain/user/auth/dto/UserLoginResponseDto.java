package com.back.domain.user.auth.dto;

import com.back.domain.user.user.dto.UserDto;

public record UserLoginResponseDto(
        UserDto user,
        String refreshToken,
        String accessToken
) {
}
