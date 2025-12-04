package com.back.domain.message.dto;

import com.back.domain.message.entity.Message;

import java.time.LocalDateTime;

public record MessageResponseDto(
        Long id,
        Long messageThreadId,
        Long senderId,
        String senderNickname,
        String profileImgUrl,
        String content,
        LocalDateTime createdAt
) {
    public MessageResponseDto(Message message) {
        this(
                message.getId(),
                message.getMessageThread().getId(),
                message.getSender().getId(),
                message.getSender().getNickname(),
                message.getSender().getProfileImgUrl(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
