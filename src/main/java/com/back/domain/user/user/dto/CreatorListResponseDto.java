package com.back.domain.user.user.dto;

import com.back.domain.user.user.entity.User;

public record CreatorListResponseDto(
        long id,
        String nickname,
        String profileImgUrl,
        long followersCount,
        boolean isFollowing
) {
    public CreatorListResponseDto(User user, boolean isFollowing) {
        this(
                user.getId(),
                user.getNickname(),
                user.getProfileImgUrl(),
                user.getFollowersCount(),
                isFollowing
        );
    }
}
