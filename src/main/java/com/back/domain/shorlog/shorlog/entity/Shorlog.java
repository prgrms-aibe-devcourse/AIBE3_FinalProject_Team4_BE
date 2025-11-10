package com.back.domain.shorlog.shorlog.entity;

import com.back.domain.user.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Shorlog extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "thumbnail_url", length = 255, nullable = false)
    private String thumbnailUrl;

    @Column(name = "thumbnail_type", length = 20, nullable = false)
    @Builder.Default
    private String thumbnailType = "upload";

    @Column(name = "view_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer viewCount = 0;

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void update(String content, String thumbnailUrl, String thumbnailType) {
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.thumbnailType = thumbnailType;
    }
}