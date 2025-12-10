package com.back.domain.history.history.repository;

import com.back.domain.history.history.dto.CreatorOverviewDto;

import java.time.LocalDateTime;

public interface CreatorDashboardQueryRepository {
    CreatorOverviewDto getOverview(Long creatorId, LocalDateTime since);
}