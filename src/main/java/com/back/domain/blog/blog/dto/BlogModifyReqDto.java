package com.back.domain.blog.blog.dto;

import jakarta.validation.Valid;

import java.util.List;

public record BlogModifyReqDto(
        @Valid BlogModifyDto blog,
        List<Long> hashtagIds
) {
}
