package com.back.domain.blog.bookmark.entity;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.user.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "blog_bookmarks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_blog_user",
                        columnNames = {"blog_id", "user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_user_created", columnList = "user_id, bookmarked_at DESC"),
                @Index(name = "idx_blog_id", columnList = "blog_id")
        })
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class BlogBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연관관계 편의 메서드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @CreatedDate
    @Column(name = "bookmarked_at", updatable = false, nullable = false)
    private LocalDateTime bookmarkedAt;

    public BlogBookmark(Blog blog, User user) {
        this.blog = blog;
        this.user = user;
    }


}