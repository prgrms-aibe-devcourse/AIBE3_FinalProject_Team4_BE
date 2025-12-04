package com.back.domain.message.dto;

public record TmpMessageRequestDto(
        Long meId,
        Long messageThreadId,
        String content
) {
}
