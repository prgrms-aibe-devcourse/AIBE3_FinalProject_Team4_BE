package com.back.domain.user.user.dto;

public record UserLoginResponseDto(
        UserDto user,
        String apiKey
) {
}
