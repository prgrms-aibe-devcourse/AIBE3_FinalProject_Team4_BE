package com.back.domain.message.dto;

public record MessageRequestDto (
        Long messageThreadId,
        String content
) {
}
