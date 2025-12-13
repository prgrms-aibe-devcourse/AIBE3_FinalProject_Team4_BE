package com.back.domain.history.history.repository;

import com.back.domain.history.history.dto.CreatorPeriodStats;
import com.back.domain.history.history.dto.CreatorTotalStats;
import com.back.domain.history.history.dto.DailyContentViewsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CreatorDashboardQueryRepository {
    // 전체 누적 통계
    CreatorTotalStats getTotalStats(Long creatorId);

    // 특정 기간(from ~ to) 통계
    CreatorPeriodStats getPeriodStats(Long creatorId, LocalDateTime from, LocalDateTime to);

    // 최근 30일 일별 콘텐츠 조회수
    List<DailyContentViewsDto> getDailyViews30d(
            Long creatorId,
            LocalDateTime from,
            LocalDateTime to
    );
}