package com.back.domain.user.mail.dto;

public record EmailVerifyRequestDto (
        String email,
        String code
) {}
