package com.back.domain.shorlog.shorlogbookmark.entity;

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
@IdClass(ShorlogBookmark.ShorlogBookmarkId.class) // 복합 키
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShorlogBookmark {  // BaseEntity 상속 X
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
    private LocalDateTime createdAt;

    public static ShorlogBookmark create(Shorlog shorlog, User user) {
        ShorlogBookmark bookmark = new ShorlogBookmark();
        bookmark.shorlog = shorlog;
        bookmark.user = user;
        return bookmark;
    }

    // 복합 키 클래스
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class ShorlogBookmarkId implements Serializable {
        private Long shorlog;
        private Long user;
    }
}
