package com.back.domain.shorlog.shorlogdraft.entity;

import com.back.domain.user.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import com.back.global.util.JsonUtil;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShorlogDraft extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "thumbnail_urls", columnDefinition = "JSON")
    private String thumbnailUrls;

    @Column(name = "hashtags", columnDefinition = "JSON")
    private String hashtags;

    public static ShorlogDraft create(User user, String content, List<String> thumbnailUrls, List<String> hashtags) {
        ShorlogDraft draft = new ShorlogDraft();
        draft.user = user;
        draft.content = content;
        draft.setThumbnailUrlList(thumbnailUrls);
        draft.setHashtagList(hashtags);
        return draft;
    }

    public void update(String content, List<String> thumbnailUrls, String hashtags) {
        this.content = content;
        setThumbnailUrlList(thumbnailUrls);
        this.hashtags = hashtags;
    }

    // Helper 메서드: JSON → List<String>
    public List<String> getThumbnailUrlList() {
        return JsonUtil.toStringList(thumbnailUrls);
    }

    // Helper 메서드: List<String> → JSON
    public void setThumbnailUrlList(List<String> urls) {
        this.thumbnailUrls = JsonUtil.toJson(urls);
    }

    // Helper 메서드: List<String> → JSON (해시태그)
    public void setHashtagList(List<String> hashtagList) {
        this.hashtags = JsonUtil.toJson(hashtagList);
    }
}