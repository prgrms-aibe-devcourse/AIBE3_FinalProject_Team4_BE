package com.back.domain.blog.blog.entity;


import com.back.domain.blog.blog.dto.BlogWriteDto;
import com.back.domain.blog.hashtag.BlogHashtag;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "blogs")
public class Blog {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private Long userId;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime modifiedAt;

    private String thumbnailUrl;
    private Integer viewCount = 0;
    private Integer likeCount = 0;
    private Integer bookmarkCount = 0;
    private Integer commentCount = 0;

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlogHashtag> hashtags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BlogStatus status = BlogStatus.DRAFT;

    public static Blog create(@Valid BlogWriteDto blogDto, List<Long> hashtagIds) {
        if (blogDto.title() == null || blogDto.title().isBlank()) {
            throw new IllegalArgumentException("Blog title cannot be null or empty");
        }

        Blog blog = Blog.builder()
                .title(blogDto.title())
                .content(blogDto.content())
                .thumbnailUrl(blogDto.thumbnailUrl())
                .status(blogDto.status())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .viewCount(0)
                .likeCount(0)
                .bookmarkCount(0)
                .commentCount(0)
                .build();
        blog.updateHashtags(hashtagIds);

        return blog;
    }

    public void publish() {
        if (this.status == BlogStatus.PUBLISHED) {
            throw new IllegalStateException("Already published");
        }
        this.status = BlogStatus.PUBLISHED;
    }

    public void unpublish() {
        if (this.status == BlogStatus.DRAFT) {
            throw new IllegalStateException("Cannot unpublish draft");
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

    public void modify(BlogWriteDto blogDto, List<Long> hashtagIds) {
        this.title = blogDto.title();
        this.content = blogDto.content();
        this.thumbnailUrl = blogDto.thumbnailUrl();
        this.status = blogDto.status();
        this.modifiedAt = LocalDateTime.now();

        this.updateHashtags(hashtagIds);
    }

    public void increaseLikeCount() {
        this.likeCount += 1;
    }

    private void updateHashtags(List<Long> hashtagIds) {
        if (this.hashtags != null) {
            this.hashtags.clear();
        } else {
            this.hashtags = new ArrayList<>();
        }

        if (hashtagIds != null) {
            hashtagIds.stream()
                    .map(id -> new BlogHashtag(id, this))
                    .forEach(this.hashtags::add);
        }
    }
}