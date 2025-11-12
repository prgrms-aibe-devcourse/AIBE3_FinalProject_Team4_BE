package com.back.domain.blog.like.entity;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.user.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "blog_like",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_blog_user",
                        columnNames = {"blog_id", "user_id"}
                )
        })
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BlogLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Blog blog;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime likedAt;

    @Builder
    public BlogLike(Blog blog, User user) {
        this.blog = blog;
        this.user = user;
    }
}
