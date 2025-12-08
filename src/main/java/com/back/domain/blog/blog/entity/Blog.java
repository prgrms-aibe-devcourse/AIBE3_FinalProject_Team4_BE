package com.back.domain.blog.blog.entity;

import com.back.domain.blog.blog.dto.BlogWriteReqDto;
import com.back.domain.blog.blog.exception.BlogErrorCase;
import com.back.domain.blog.blogFile.entity.BlogFile;
import com.back.domain.blog.bloghashtag.entity.BlogHashtag;
import com.back.domain.blog.bookmark.entity.BlogBookmark;
import com.back.domain.blog.like.entity.BlogLike;
import com.back.domain.shared.hashtag.entity.Hashtag;
import com.back.domain.user.user.entity.User;
import com.back.global.exception.ServiceException;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "blogs")
public class Blog extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;

    private String thumbnailUrl;
    private long viewCount = 0;
    private long likeCount = 0;
    private long bookmarkCount = 0;

    @OneToMany(mappedBy = "blog", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<BlogHashtag> blogHashtags = new ArrayList<>();

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlogLike> likes = new ArrayList<>();

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlogBookmark> bookmark = new ArrayList<>();

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlogFile> blogFiles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BlogStatus status = BlogStatus.DRAFT;

    public Blog(User user, String title, String content, BlogStatus status) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.status = status;
        this.blogHashtags = new ArrayList<>();
        this.viewCount = 0;
        this.likeCount = 0;
        this.bookmarkCount = 0;
    }

    public static Blog create(User user, String title, String content, BlogStatus status) {
        if (title == null || title.isBlank()) {
            throw new ServiceException(BlogErrorCase.INVALID_FORMAT);
        }
        Blog blog = new Blog(user, title, content, status);
        return blog;
    }

    public void updateHashtags(List<Hashtag> hashtags) {
        this.blogHashtags.clear();
        Set<Long> uniqueIds = new HashSet<>();
        for (Hashtag hashtag : hashtags) {
            if (uniqueIds.add(hashtag.getId())) {
                this.blogHashtags.add(new BlogHashtag(this, hashtag));
            }
        }
    }

    public void publish() {
        this.status = BlogStatus.PUBLISHED;
    }

    public void unpublish() {
        this.status = BlogStatus.DRAFT;
    }

    public boolean isPublished() {
        return this.status == BlogStatus.PUBLISHED;
    }

    public void increaseViewCount() {
        this.viewCount += 1;
    }

    public void modify(BlogWriteReqDto reqBody) {
        this.title = reqBody.title();
        this.content = reqBody.content();
        this.status = reqBody.status();
    }

    public void changeThumbnailUrl(String s3Url) {
        this.thumbnailUrl = s3Url;
    }

}