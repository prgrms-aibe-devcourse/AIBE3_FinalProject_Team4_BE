package com.back.domain.user.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequestDto(
        @NotBlank
        @Size(min = 4, max = 30)
        String nickname,

        String bio,

        String profileImgUrl
) {
}
