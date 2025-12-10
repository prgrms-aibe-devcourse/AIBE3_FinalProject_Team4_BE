package com.back.domain.history.history.dto;

public record CreatorOverviewDto(
        long totalViews,
        long totalLikes,
        long totalBookmarks,
        long followerCount,
        long recentLikes,
        long recentBookmarks,
        long recentComments,
        long recentFollowers
) {
}