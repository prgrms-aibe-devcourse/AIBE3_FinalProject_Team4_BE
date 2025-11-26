package com.back.domain.user.user.dto;

import com.back.domain.user.user.entity.User;

public record UserListResponseDto (
        long id,
        String nickname,
        String profileImgUrl,
        String bio,

        long followersCount
){
    public UserListResponseDto(User user, long followersCount) {
        this(
                user.getId(),
                user.getNickname(),
                user.getProfileImgUrl(),
                user.getBio(),
                followersCount
        );
    }

    public UserListResponseDto(User user) {
        this(
                user.getId(),
                user.getNickname(),
                user.getProfileImgUrl(),
                user.getBio(),
                user.getFollowersCount()
        );
    }
}
