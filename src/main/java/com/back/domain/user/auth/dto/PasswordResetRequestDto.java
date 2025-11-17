package com.back.domain.user.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequestDto (

        @NotBlank
        String email,

        @NotBlank
        String username,

        @NotBlank
        @Size(min = 4, max = 30)
        String newPassword,

        @NotBlank(message = "이메일 인증 토큰은 필수입니다.")
        String verificationToken
) {
}