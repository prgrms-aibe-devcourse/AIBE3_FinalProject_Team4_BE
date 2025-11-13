package com.back.domain.user.user.dto;

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
        @JsonFormat(pattern = "yyyyMMdd")
        LocalDate dateOfBirth,

        @NotNull
        Gender gender
) {
}
