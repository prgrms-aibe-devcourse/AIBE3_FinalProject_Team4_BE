package com.back.domain.shorlog.shorlog.entity;

import com.back.domain.shorlog.shorloghashtag.entity.ShorlogHashtag;
import com.back.domain.shorlog.shorlogimage.entity.ShorlogImages;
import com.back.domain.user.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
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

    @OneToMany(mappedBy = "shorlog", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<ShorlogImages> images = new ArrayList<>();

    @OneToMany(mappedBy = "shorlog", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ShorlogHashtag> hashtags = new ArrayList<>();

    @Column(name = "view_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "tts_url")
    private String ttsUrl;

    public void update(String content) {
        this.content = content;
    }

    public void updateTtsUrl(String ttsUrl) {
        this.ttsUrl = ttsUrl;
    }

    public List<String> getThumbnailUrlList() {
        return images.stream()
                .map(si -> si.getImage().getS3Url())
                .toList();
    }
}