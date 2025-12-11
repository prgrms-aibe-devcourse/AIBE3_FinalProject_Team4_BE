package com.back.domain.history.history.repository;

import com.back.domain.history.history.dto.CreatorPeriodStats;
import com.back.domain.history.history.dto.CreatorTotalStats;

import java.time.LocalDateTime;

public interface CreatorDashboardQueryRepository {
    // 전체 누적 통계
    CreatorTotalStats getTotalStats(Long creatorId);

    // 특정 기간(from ~ to) 통계
    CreatorPeriodStats getPeriodStats(Long creatorId, LocalDateTime from, LocalDateTime to);
}