package com.back.domain.shorlog.shorlogtts.repository;

import com.back.domain.shorlog.shorlogtts.entity.ShorlogTtsUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShorlogTtsUsageRepository extends JpaRepository<ShorlogTtsUsage, ShorlogTtsUsage.ShorlogTtsUsageId> {

    boolean existsByShorlogIdAndUserId(Long shorlogId, Long userId);
}
