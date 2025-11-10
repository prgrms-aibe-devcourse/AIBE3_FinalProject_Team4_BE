package com.back.domain.shorlog.shorloglike.entity;

import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.domain.user.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@IdClass(ShorlogLike.ShorlogLikeId.class) // 복합 키
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ShorlogLike {  // BaseEntity 상속 X
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shorlog_id", nullable = false)
    private Shorlog shorlog;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createDate;

    // 복합 키 클래스
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class ShorlogLikeId implements Serializable {
        private Long shorlog;
        private Long user;
    }
}
