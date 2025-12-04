package com.back.domain.message.dto;

import com.back.domain.message.entity.Message;

import java.time.LocalDateTime;

public record MessageDto (
        Long id,
        Long senderId,
        String senderNickname,
        String profileImgUrl,
        String content,
        LocalDateTime createdAt
) {
    public MessageDto(Message message) {
        this(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getNickname(),
                message.getSender().getProfileImgUrl(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
