package com.back.domain.shorlog.shorlogtts.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "shorlog_tts_usage")
@IdClass(ShorlogTtsUsage.ShorlogTtsUsageId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShorlogTtsUsage {

    @Id
    @Column(name = "shorlog_id")
    private Long shorlogId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;

    public static ShorlogTtsUsage create(Long shorlogId, Long userId) {
        ShorlogTtsUsage usage = new ShorlogTtsUsage();
        usage.shorlogId = shorlogId;
        usage.userId = userId;
        usage.usedAt = LocalDateTime.now();
        return usage;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ShorlogTtsUsageId implements Serializable {
        private Long shorlogId;
        private Long userId;
    }
}

