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
        int blogsCount,

        boolean isFollowing,   // 내가 이 유저를 팔로우 중인지
        boolean isFollower     // 이 유저가 나를 팔로우 중인지
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
                blogsCount,

                false, // isFollowing 기본값
                false  // isFollower 기본값
        );
    }

    // 팔로우 관계를 반영한 DTO 생성용 정적 메서드
    public static ProfileResponseDto withFollowStatus(
            ProfileResponseDto base,
            boolean isFollowing,
            boolean isFollower
    ) {
        return new ProfileResponseDto(
                base.id(),
                base.nickname(),
                base.profileImgUrl(),
                base.bio(),
                base.followersCount(),
                base.followingCount(),
                base.likesCount(),
                base.shorlogsCount(),
                base.blogsCount(),
                isFollowing,
                isFollower
        );
    }
}
