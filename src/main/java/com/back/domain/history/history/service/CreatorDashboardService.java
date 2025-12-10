package com.back.domain.history.history.service;

import com.back.domain.history.history.dto.CreatorOverviewDto;
import com.back.domain.history.history.repository.CreatorDashboardQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreatorDashboardService {

    private static final long RECENT_DAYS = 7L;

    private final CreatorDashboardQueryRepository creatorDashboardQueryRepository;

    public CreatorOverviewDto getOverview(Long creatorId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime since = now.minusDays(RECENT_DAYS);

        return creatorDashboardQueryRepository.getOverview(creatorId, since);
    }
}