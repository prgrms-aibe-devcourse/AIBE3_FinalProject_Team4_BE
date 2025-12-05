package com.back.domain.shorlog.shorlog.entity;

import com.back.domain.shorlog.shorlogbookmark.entity.ShorlogBookmark;
import com.back.domain.shorlog.shorloghashtag.entity.ShorlogHashtag;
import com.back.domain.shorlog.shorlogimage.entity.ShorlogImages;
import com.back.domain.shorlog.shorloglike.entity.ShorlogLike;
import com.back.domain.user.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shorlog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @OneToMany(mappedBy = "shorlog", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<ShorlogImages> images = new ArrayList<>();

    @OneToMany(mappedBy = "shorlog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShorlogHashtag> hashtags = new ArrayList<>();

    @OneToMany(mappedBy = "shorlog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShorlogLike> likes = new ArrayList<>();

    @OneToMany(mappedBy = "shorlog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShorlogBookmark> bookmarks = new ArrayList<>();

    @Column(name = "view_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer viewCount = 0;

    @Column(name = "tts_url")
    private String ttsUrl;

    @Column(name = "tts_creator_id")
    private Long ttsCreatorId;

    public static Shorlog create(User user, String content) {
        Shorlog shorlog = new Shorlog();
        shorlog.user = user;
        shorlog.content = content;
        shorlog.images = new ArrayList<>();
        shorlog.hashtags = new ArrayList<>();
        shorlog.viewCount = 0;
        return shorlog;
    }

    public void update(String content) {
        this.content = content;
    }

    public void updateTtsUrl(String ttsUrl) {
        this.ttsUrl = ttsUrl;
    }

    public void updateTtsCreatorId(Long ttsCreatorId) {
        this.ttsCreatorId = ttsCreatorId;
    }

    public List<String> getThumbnailUrlList() {
        return images.stream()
                .map(si -> si.getImage().getS3Url())
                .toList();
    }
}