package com.back.domain.shorlog.shorloglike.entity;

import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.domain.user.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// 단일 ID + UniqueConstraint 방식 사용
@Entity
@Table(name = "shorlog_like",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_shorlog_user",
                        columnNames = {"shorlog_id", "user_id"}
                )
        })
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShorlogLike {
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
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ShorlogLike create(Shorlog shorlog, User user) {
        ShorlogLike like = new ShorlogLike();
        like.shorlog = shorlog;
        like.user = user;
        return like;
    }
}
