package com.back.domain.user.user.dto;

import com.back.domain.user.user.entity.User;

public record FullCreatorListResponseDto(
        long id,
        String nickname,
        String profileImgUrl,
        long followersCount,
        boolean isFollowing,
        String popularThumbnailUrl
) {
    public FullCreatorListResponseDto(User user, boolean isFollowing, String popularThumbnailUrl) {
        this(
                user.getId(),
                user.getNickname(),
                user.getProfileImgUrl(),
                user.getFollowersCount(),
                isFollowing,
                popularThumbnailUrl
        );
    }
}
