package com.back.domain.message.dto;

public record MessageDto (
        Long id,
        Long senderId,
        String senderNickname,
        String profileImgUrl,
        String content
) {
}
