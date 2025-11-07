package com.back.project.domain.shorlog.shorlogdraft.entity;

import com.back.project.domain.user.user.entity.User;
import com.back.project.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ShorlogDraft extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "thumbnail_url", length = 255)
    private String thumbnailUrl;

    @Column(name = "hashtags", columnDefinition = "JSON")
    private String hashtags;

    public void update(String content, String thumbnailUrl, String hashtags) {
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.hashtags = hashtags;
    }
}