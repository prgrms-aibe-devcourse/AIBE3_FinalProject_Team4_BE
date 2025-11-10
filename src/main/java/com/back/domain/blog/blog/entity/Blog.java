package com.back.domain.blog.blog.entity;


import com.back.domain.blog.blog.dto.BlogWriteReqDto;
import com.back.domain.blog.hashtag.entity.BlogHashtag;
import com.back.global.exception.ServiceException;
import jakarta.persistence.*;
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

    @OneToMany(mappedBy = "blog", orphanRemoval = true, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private List<BlogHashtag> blogHashtags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BlogStatus status = BlogStatus.DRAFT;

    public void publish() {
        if (this.status == BlogStatus.PUBLISHED) {
            throw new ServiceException("404-1", "이미 게시된 글입니다.");
        }
        this.status = BlogStatus.PUBLISHED;
    }

    public void unpublish() {
        if (this.status == BlogStatus.DRAFT) {
            throw new ServiceException("404-1", "이미 임시저장된 글입니다.");
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

    public void modify(BlogWriteReqDto reqBody, List<Long> hashtagIds) {
        this.title = reqBody.title();
        this.content = reqBody.content();
        this.thumbnailUrl = reqBody.thumbnailUrl();
        this.status = reqBody.status();
        this.modifiedAt = LocalDateTime.now();

        this.updateHashtags(hashtagIds);
    }

    public void increaseLikeCount() {
        this.likeCount += 1;
    }

    public void updateHashtags(List<Long> hashtagIds) {
        if (this.blogHashtags != null) {
            this.blogHashtags.clear();
        } else {
            this.blogHashtags = new ArrayList<>();
        }

        if (hashtagIds != null) {
            hashtagIds.stream()
                    .map(id -> new BlogHashtag(id, this))
                    .forEach(this.blogHashtags::add);
        }
    }
}