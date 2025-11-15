package com.back.domain.blog.blog.entity;


import com.back.domain.blog.blog.dto.BlogWriteReqDto;
import com.back.domain.blog.blog.exception.BlogErrorCase;
import com.back.domain.blog.bloghashtag.entity.BlogHashtag;
import com.back.domain.blog.bookmark.entity.BlogBookmark;
import com.back.domain.blog.like.entity.BlogLike;
import com.back.domain.shared.hashtag.entity.Hashtag;
import com.back.domain.user.user.entity.User;
import com.back.global.exception.ServiceException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "blogs")
public class Blog {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime modifiedAt;

    private String thumbnailUrl;
    private long viewCount = 0;
    private long likeCount = 0;
    private long bookmarkCount = 0;
    private long commentCount = 0;

    @OneToMany(mappedBy = "blog", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<BlogHashtag> blogHashtags = new ArrayList<>();

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlogLike> likes = new ArrayList<>();

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlogBookmark> bookmark = new ArrayList<>();

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BlogStatus status = BlogStatus.DRAFT;


    public Blog(User user, String title, String content, String thumbnailUrl, BlogStatus status) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.status = status;
        this.blogHashtags = new ArrayList<>();
        this.viewCount = 0;
        this.likeCount = 0;
        this.bookmarkCount = 0;
        this.commentCount = 0;
    }

    public static Blog create(User user, String title, String content, String thumbnailUrl, BlogStatus status) {
        if (title == null || title.isBlank()) {
            throw new ServiceException(BlogErrorCase.INVALID_FORMAT);
        }
        Blog blog = new Blog(user, title, content, thumbnailUrl, status);
        return blog;
    }

    public void updateHashtags(List<Hashtag> hashtags) {
        this.blogHashtags.clear();

        for (Hashtag hashtag : hashtags) {
            BlogHashtag blogHashtag = new BlogHashtag(this, hashtag);
            this.blogHashtags.add(blogHashtag);
        }
    }

    public void publish() {
        if (this.status == BlogStatus.PUBLISHED) {
            throw new ServiceException(BlogErrorCase.INVALID_FORMAT);
        }
        this.status = BlogStatus.PUBLISHED;
    }

    public void unpublish() {
        if (this.status == BlogStatus.DRAFT) {
            throw new ServiceException(BlogErrorCase.INVALID_FORMAT);
        }
        this.status = BlogStatus.DRAFT;
    }

    public boolean isPublished() {
        return this.status == BlogStatus.PUBLISHED;
    }

    public boolean isDraft() {
        return this.status == BlogStatus.DRAFT;
    }

    public void increaseViewCount() {
        this.viewCount += 1;
    }

    public void modify(BlogWriteReqDto reqBody) {
        this.title = reqBody.title();
        this.content = reqBody.content();
        this.thumbnailUrl = reqBody.thumbnailUrl();
        this.status = reqBody.status();
        this.modifiedAt = LocalDateTime.now();
    }
}