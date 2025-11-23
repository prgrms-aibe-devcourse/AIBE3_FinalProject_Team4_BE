package com.back.domain.user.activity.dto;

import com.back.domain.blog.bookmark.entity.BlogBookmark;
import com.back.domain.blog.like.entity.BlogLike;
import com.back.domain.shorlog.shorlogbookmark.entity.ShorlogBookmark;
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

    public UserActivityDto(BlogLike blogLike) {
        this(
                blogLike.getBlog().getId(),
                blogLike.getLikedAt()
        );
    }

    public UserActivityDto(ShorlogBookmark shorlogBookmark) {
        this(
                shorlogBookmark.getShorlog().getId(),
                shorlogBookmark.getCreatedAt()
        );
    }

    public UserActivityDto(BlogBookmark blogBookmark) {
        this(
                blogBookmark.getBlog().getId(),
                blogBookmark.getBookmarkedAt()
        );
    }
}
