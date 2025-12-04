package com.back.domain.message.dto;

import com.back.domain.user.user.entity.User;

import java.util.List;

public record MessageThreadResponseDto(
        Long messageThreadId,
        Long otherUserId,
        String otherUserNickname,
        String otherUserProfileImgUrl,
        List<MessageDto> messages
) {
    public MessageThreadResponseDto(Long messageThreadId, User otherUser, List<MessageDto> messages) {
        this(
                messageThreadId,
                otherUser.getId(),
                otherUser.getNickname(),
                otherUser.getProfileImgUrl(),
                messages
        );
    }
}
