package com.back.domain.message.dto;

import com.back.domain.message.entity.Message;
import com.back.domain.user.user.entity.User;

import java.util.List;

public record MessageThreadResponseDto(
        Long messageThreadId,
        Long OtherUserId,
        String OtherUserNickname,
        String OtherUserProfileImgUrl,
        List<Message> messages
) {
    public MessageThreadResponseDto(Long messageThreadId, User otherUser, List<Message> messages) {
        this(
                messageThreadId,
                otherUser.getId(),
                otherUser.getNickname(),
                otherUser.getProfileImgUrl(),
                messages
        );
    }
}
