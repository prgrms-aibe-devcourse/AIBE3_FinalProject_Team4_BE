package com.back.domain.user.follow.dto;

import com.back.domain.user.user.entity.User;

public record UserProfileWithFollowStatusResponseDto(
        Long userId,
        String username,
        String nickname,
        String profileImgUrl,
        String bio,
        long followersCount,
        long followingsCount,
        boolean isFollowing,
        boolean isFollower
) {
    public static UserProfileWithFollowStatusResponseDto of(
            User target,
            long followersCount,
            long followingsCount,
            boolean isFollowing,
            boolean isFollower
    ){
        return new UserProfileWithFollowStatusResponseDto(
                target.getId(),
                target.getUsername(),
                target.getNickname(),
                target.getProfileImgUrl(),
                target.getBio(),
                followersCount,
                followingsCount,
                isFollowing,
                isFollower
        );
    }
}
