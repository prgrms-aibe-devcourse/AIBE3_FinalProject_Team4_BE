package com.back.domain.message.dto;

public record ReadMessageThreadResponseDto(
        Long messageThreadId,
        Long lastReadMessageId
) {}
