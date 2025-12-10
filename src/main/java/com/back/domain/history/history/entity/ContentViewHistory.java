package com.back.domain.history.history.entity;

import com.back.domain.main.entity.ContentType;
import com.back.domain.user.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "content_view_history")
@NoArgsConstructor
@AllArgsConstructor
public class ContentViewHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ContentType contentType;

    private Long contentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viewer_id")
    private User viewer; // 비로그인 null

    private LocalDateTime createdAt;

    private String ip;
    private String userAgent;

    public ContentViewHistory(ContentType contentType,
                              Long contentId,
                              User viewer,
                              LocalDateTime createdAt,
                              String ip,
                              String userAgent) {
        this.contentType = contentType;
        this.contentId = contentId;
        this.viewer = viewer;
        this.createdAt = createdAt;
        this.ip = ip;
        this.userAgent = userAgent;
    }
}