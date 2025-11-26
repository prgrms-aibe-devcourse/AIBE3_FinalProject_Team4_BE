package com.back.domain.user.follow.dto;

public record FollowCountResponseDto(
    long followerCount,
    long followingCount
) {
}
