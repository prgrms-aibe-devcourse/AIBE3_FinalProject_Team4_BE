package com.back.domain.blog.bloghashtag.repository;

import com.back.domain.blog.bloghashtag.entity.BlogHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BlogHashTagRepository extends JpaRepository<BlogHashtag, Long> {
    @Query("""
        SELECT bh.hashtag.name, COUNT(bh)
        FROM BlogHashtag bh
        WHERE bh.blog.createdAt >= :from
          AND bh.blog.status = 'PUBLISHED'
        GROUP BY bh.hashtag.name
        """)
    List<Object[]> countHashtagUsageSince(LocalDateTime from);
}
