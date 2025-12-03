package com.back.domain.message.dto;

import com.back.domain.message.entity.MessageThread;
import com.back.domain.user.user.entity.User;

import java.time.LocalDateTime;

public record MessageThreadListResponseDto(
        Long messageThreadId,
        Long otherUserId,
        String otherUserNickname,
        String otherUserProfileImgUrl,
        String lastMessageContent,
        LocalDateTime lastMessageCreatedAt
) {
    public MessageThreadListResponseDto(MessageThread messageThread, User otherUser, String lastMessageContent, LocalDateTime lastMessageCreatedAt) {
        this(
                messageThread.getId(),
                otherUser.getId(),
                otherUser.getNickname(),
                otherUser.getProfileImgUrl(),
                lastMessageContent,
                lastMessageCreatedAt
        );
    }
}
