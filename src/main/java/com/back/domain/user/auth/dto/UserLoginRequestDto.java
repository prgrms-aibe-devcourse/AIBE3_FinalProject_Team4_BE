package com.back.domain.user.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserLoginRequestDto(
        @NotBlank
        @Size(min = 4, max = 20)
        String username,
        @NotBlank
        @Size(min = 4, max = 30)
        String password
) {}
