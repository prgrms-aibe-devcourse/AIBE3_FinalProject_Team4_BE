package com.back.domain.recommend.recommend;

import com.back.domain.shorlog.shorloglike.entity.ShorlogLike;

import java.time.LocalDateTime;

public record UserActivityDto(
        Long postId,
        LocalDateTime activityAt
) {
    public UserActivityDto(ShorlogLike shorlogLike) {
        this(
                shorlogLike.getShorlog().getId(),
                shorlogLike.getCreatedAt()
        );
    }
}
