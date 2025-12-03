package com.back.domain.message.dto;

import com.back.domain.message.entity.MessageThread;
import com.back.domain.user.user.entity.User;

public record CreateMessageThreadResponseDto (
        Long messageThreadId,
        Long otherUserId,
        String otherUserNickname,
        String otherUserProfileImgUrl
) {
    public CreateMessageThreadResponseDto(MessageThread messageThread, User otherUser) {
        this(
                messageThread.getId(),
                otherUser.getId(),
                otherUser.getNickname(),
                otherUser.getProfileImgUrl()
        );
    }
}