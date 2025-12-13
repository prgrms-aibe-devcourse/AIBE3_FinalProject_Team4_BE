package com.back.domain.history.history.dto;

import java.util.List;

public record CreatorOverviewDto(
        int periodDays,
        // 전체 누적
        long totalViews,
        long totalLikes,
        long totalBookmarks,
        long totalComments,
        long followerCount,
        // 최근 N일 간
        long periodViews,
        long periodLikes,
        long periodBookmarks,
        long periodComments,
        long periodFollowers,
        // 퍼포먼스 요약
        double likeRate,          // 좋아요율 (기간 좋아요 / 기간 조회수 * 100)
        double bookmarkRate,      // 북마크율
        double viewsPerFollower,  // 팔로워 1명당 조회수
        // 전 기간 대비 변화율(%) – 없으면 null
        Double viewsChangeRate,
        Double likesChangeRate,
        Double bookmarksChangeRate,
        Double followersChangeRate,

        // 일별 콘텐츠 조회수 (최근 30일)
        List<DailyContentViewsDto> dailyViews30d
) {
}