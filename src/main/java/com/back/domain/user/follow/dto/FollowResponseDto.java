package com.back.domain.user.follow.dto;

import com.back.domain.user.user.entity.User;

public record FollowResponseDto(
        long id,
        String nickname,
        String profileImgUrl,
        String bio,
        boolean isFollowing
) {
    public FollowResponseDto(User user, boolean isFollowing) {
        this(
                user.getId(),
                user.getNickname(),
                user.getProfileImgUrl(),
                user.getBio(),
                isFollowing
        );
    }
}
