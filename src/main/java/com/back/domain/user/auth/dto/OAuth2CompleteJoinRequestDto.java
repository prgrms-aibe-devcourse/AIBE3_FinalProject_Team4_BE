package com.back.domain.user.auth.dto;

import com.back.domain.user.user.entity.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record OAuth2CompleteJoinRequestDto(
        @NotBlank
        @Size(min = 4, max = 30)
        String nickname,

        @NotNull
        LocalDate dateOfBirth,

        @NotNull
        Gender gender,

        @NotBlank(message = "발급받은 임시 토큰을 포함해주세요.")
        String temporaryToken
) {
}
