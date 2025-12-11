package com.back.domain.history.history.service;

import com.back.domain.history.history.dto.CreatorOverviewDto;
import com.back.domain.history.history.dto.CreatorPeriodStats;
import com.back.domain.history.history.dto.CreatorTotalStats;
import com.back.domain.history.history.repository.CreatorDashboardQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreatorDashboardService {

    private final CreatorDashboardQueryRepository creatorDashboardQueryRepository;

    @Transactional(readOnly = true)
    public CreatorOverviewDto getOverview(Long creatorId, int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodStart = now.minusDays(days);
        LocalDateTime prevPeriodStart = now.minusDays(days * 2);

        CreatorTotalStats total = creatorDashboardQueryRepository.getTotalStats(creatorId);
        CreatorPeriodStats current =
                creatorDashboardQueryRepository.getPeriodStats(creatorId, periodStart, now);
        CreatorPeriodStats previous =
                creatorDashboardQueryRepository.getPeriodStats(creatorId, prevPeriodStart, periodStart);

        double likeRate = calcRate(current.views(), current.likes());
        double bookmarkRate = calcRate(current.views(), current.bookmarks());
        double viewsPerFollower = total.followerCount() == 0
                ? 0.0
                : (double) current.views() / total.followerCount();

        return new CreatorOverviewDto(
                days,

                // 전체 누적
                total.views(),
                total.likes(),
                total.bookmarks(),
                total.followerCount(),

                // 기간 활동
                current.views(),
                current.likes(),
                current.bookmarks(),
                current.comments(),
                current.followers(),

                // 퍼포먼스 요약
                likeRate,
                bookmarkRate,
                viewsPerFollower,

                // 변화율
                calcChangeRate(previous.views(), current.views()),
                calcChangeRate(previous.likes(), current.likes()),
                calcChangeRate(previous.bookmarks(), current.bookmarks()),
                calcChangeRate(previous.followers(), current.followers())
        );
    }

    private double calcRate(long base, long part) {
        if (base <= 0) return 0.0;
        return (double) part * 100.0 / base;
    }

    private Double calcChangeRate(long previous, long current) {
        if (previous <= 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((double) (current - previous) * 100.0) / previous;
    }
}