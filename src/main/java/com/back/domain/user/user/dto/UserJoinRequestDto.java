package com.back.domain.user.user.dto;

import com.back.domain.user.user.entity.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserJoinRequestDto(
        @NotBlank
        String email,

        @NotBlank
        @Size(min = 4, max = 20)
        String username,

        @NotBlank
        @Size(min = 4, max = 30)
        String password,

        @NotBlank
        @Size(min = 4, max = 30)
        String nickname,

        @NotNull
        LocalDate dateOfBirth,

        @NotNull
        Gender gender,

        @NotBlank(message = "이메일 인증 토큰은 필수입니다.")
        String verificationToken
) {
}

