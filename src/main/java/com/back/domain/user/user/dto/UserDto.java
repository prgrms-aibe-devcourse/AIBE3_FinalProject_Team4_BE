package com.back.domain.user.user.dto;

import com.back.domain.user.user.entity.User;

import java.time.LocalDateTime;

public record UserDto(
    long id,
    String username,
    String nickname,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt
) {
    public UserDto(User user) {
        this(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getCreatedAt(),
                user.getModifiedAt()
        );
    }
}