package com.back.domain.shorlog.shorlog.entity;

import com.back.domain.user.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import com.back.global.util.JsonUtil;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

    @Column(name = "thumbnail_urls", columnDefinition = "JSON", nullable = false)
    private String thumbnailUrls;

    @Column(name = "view_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer viewCount = 0;

    public void update(String content, List<String> thumbnailUrls) {
        this.content = content;
        setThumbnailUrlList(thumbnailUrls);
    }

    // Helper 메서드: JSON → List<String>
    public List<String> getThumbnailUrlList() {
        return JsonUtil.toStringList(thumbnailUrls);
    }

    // Helper 메서드: List<String> → JSON
    public void setThumbnailUrlList(List<String> urls) {
        this.thumbnailUrls = JsonUtil.toJson(urls);
    }
}