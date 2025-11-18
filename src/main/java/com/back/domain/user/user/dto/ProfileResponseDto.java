package com.back.domain.user.user.dto;

import com.back.domain.user.user.entity.User;

public record ProfileResponseDto(
        long id,
        String nickname,
        String profileImgUrl,
        String bio,

        long followersCount,
        long followingCount,
        long likesCount,
        int shorlogsCount,
        int blogsCount
) {
    public ProfileResponseDto(User user, long followersCount, long followingCount, long likesCount,
                              int shorlogsCount, int blogsCount) {
        this(
                user.getId(),
                user.getNickname(),
                user.getProfileImgUrl(),
                user.getBio(),

                followersCount,
                followingCount,
                likesCount,
                shorlogsCount,
                blogsCount
        );
    }
}
