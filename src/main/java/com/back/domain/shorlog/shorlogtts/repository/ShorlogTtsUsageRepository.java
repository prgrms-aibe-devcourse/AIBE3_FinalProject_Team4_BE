package com.back.domain.shorlog.shorlogtts.repository;

import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.domain.shorlog.shorlogtts.entity.ShorlogTtsUsage;
import com.back.domain.user.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShorlogTtsUsageRepository extends JpaRepository<ShorlogTtsUsage, Long> {

    boolean existsByShorlogAndUser(Shorlog shorlog, User user);
}
