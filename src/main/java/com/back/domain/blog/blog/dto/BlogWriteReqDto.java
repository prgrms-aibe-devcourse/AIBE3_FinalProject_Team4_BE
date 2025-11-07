package com.back.domain.blog.blog.dto;

import jakarta.validation.Valid;

import java.util.List;

public record BlogWriteReqDto(
        @Valid BlogWriteDto blog,
        List<Long> hashtagIds
) {
}
