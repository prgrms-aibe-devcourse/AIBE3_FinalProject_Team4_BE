package com.back.domain.user.user.dto;

import com.back.domain.user.user.entity.User;

public record MyProfileResponseDto(
        long id,
        String nickname,
        String profileImgUrl,
        String bio,

        long followersCount,
        long followingCount,
        long likesCount,
        int shorlogsCount,
        int blogsCount,
        int shortlogBookmarksCount,
        int blogBookmarksCount
) {
    public MyProfileResponseDto(User user, long followersCount, long followingCount, long likesCount,
                                int shorlogsCount, int blogsCount, int shortlogBookmarksCount, int blogBookmarksCount) {
        this(
                user.getId(),
                user.getNickname(),
                user.getProfileImgUrl(),
                user.getBio(),

                followersCount,
                followingCount,
                likesCount,
                shorlogsCount,
                blogsCount,
                shortlogBookmarksCount,
                blogBookmarksCount
        );
    }
}