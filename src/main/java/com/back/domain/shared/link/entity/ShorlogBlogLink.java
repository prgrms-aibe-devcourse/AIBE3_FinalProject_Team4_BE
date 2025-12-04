package com.back.domain.shared.link.entity;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.shorlog.shorlog.entity.Shorlog;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_shorlog_blog",
                columnNames = {"shorlog_id", "blog_id"} // 같은 숏로그-블로그 조합의 중복 방지
        )
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShorlogBlogLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shorlog_id", nullable = false)
    private Shorlog shorlog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ShorlogBlogLink create(Shorlog shorlog, Blog blog) {
        ShorlogBlogLink link = new ShorlogBlogLink();
        link.shorlog = shorlog;
        link.blog = blog;
        return link;
    }
}
