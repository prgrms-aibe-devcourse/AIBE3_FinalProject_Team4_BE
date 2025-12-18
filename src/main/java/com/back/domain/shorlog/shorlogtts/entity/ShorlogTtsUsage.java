package com.back.domain.shorlog.shorlogtts.entity;

import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.domain.user.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// 단일 ID + UniqueConstraint + 엔티티 참조 방식 사용:
@Entity
@Table(name = "shorlog_tts_usage",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_shorlog_user",
                        columnNames = {"shorlog_id", "user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_user_used", columnList = "user_id, used_at DESC"),
                @Index(name = "idx_shorlog_id", columnList = "shorlog_id")
        })
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShorlogTtsUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shorlog_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Shorlog shorlog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @CreatedDate
    @Column(name = "used_at", nullable = false, updatable = false)
    private LocalDateTime usedAt;

    public static ShorlogTtsUsage create(Shorlog shorlog, User user) {
        ShorlogTtsUsage usage = new ShorlogTtsUsage();
        usage.shorlog = shorlog;
        usage.user = user;
        return usage;
    }
}

