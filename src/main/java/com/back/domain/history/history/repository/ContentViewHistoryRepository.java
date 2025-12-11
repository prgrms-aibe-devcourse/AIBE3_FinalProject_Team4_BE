package com.back.domain.history.history.repository;

import com.back.domain.history.history.entity.ContentViewHistory;
import com.back.domain.main.entity.ContentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ContentViewHistoryRepository extends JpaRepository<ContentViewHistory, Long> {
    // 크리에이터의 전체 조회수 (블로그)
    @Query("""
                select count(h)
                from ContentViewHistory h, Blog b
                where h.contentType = :type
                  and h.contentId = b.id
                  and b.user.id = :creatorId
            """)
    long countViewsForBlogAuthor(
            @Param("creatorId") Long creatorId,
            @Param("type") ContentType type  // ContentType.BLOG
    );

    // 크리에이터의 전체 조회수 (숏로그)
    @Query(""" 
                select count(h)
                from ContentViewHistory h, Shorlog s
                where h.contentType = :type
                  and h.contentId = s.id
                  and s.user.id = :creatorId
            """)
    long countViewsForShorlogAuthor(
            @Param("creatorId") Long creatorId,
            @Param("type") ContentType type  // ContentType.SHORLOG
    );

    // 크리에이터의 기간별 일별 조회수 (블로그)
    @Query("""
                select function('date', h.createdAt) as day, count(h)
                from ContentViewHistory h, Blog b
                where h.contentType = :type
                  and h.contentId = b.id
                  and b.user.id = :authorId
                  and h.createdAt between :start and :end
                group by function('date', h.createdAt)
                order by function('date', h.createdAt)
            """)
    List<Object[]> countDailyViewsForBlogAuthor(
            @Param("authorId") Long authorId,
            @Param("type") ContentType type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 크리에이터의 기간별 일별 조회수 (숏로그)
    @Query("""
                select function('date', h.createdAt) as day, count(h)
                from ContentViewHistory h, Shorlog s
                where h.contentType = :type
                  and h.contentId = s.id
                  and s.user.id = :authorId
                  and h.createdAt between :start and :end
                group by function('date', h.createdAt)
                order by function('date', h.createdAt)
            """)
    List<Object[]> countDailyViewsForShorlogAuthor(
            @Param("authorId") Long authorId,
            @Param("type") ContentType type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}