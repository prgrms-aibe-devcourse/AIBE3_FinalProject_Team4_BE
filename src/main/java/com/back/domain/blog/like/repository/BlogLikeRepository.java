package com.back.domain.blog.like.repository;

import com.back.domain.blog.like.entity.BlogLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BlogLikeRepository extends JpaRepository<BlogLike, Long> {
    Optional<BlogLike> findByBlogIdAndUserId(Long blogId, Long userId);

    // 특정 블로그들의 LIKE 여부 일괄 조회 (N+1 방지)
    @Query("""
                SELECT bl.blog.id
                FROM BlogLike bl
                WHERE bl.blog.id IN :blogIds
                  AND bl.user.id = :userId
            """)
    Set<Long> findLikedBlogIdsByUserId(
            @Param("blogIds") List<Long> blogIds,
            @Param("userId") Long userId
    );

    boolean existsByBlog_IdAndUser_Id(Long blogId, Long userId);

    long countByBlogUserId(Long blogUserId);

    // 사용자의 Like 목록 조회 (Fetch Join으로 N+1 방지)
    @EntityGraph(attributePaths = {"blog", "blog.user"})
    @Query("SELECT bm FROM BlogLike bm " +
            "WHERE bm.user.id = :userId " +
            "ORDER BY bm.likedAt DESC")
    Page<BlogLike> findByUserIdWithBlog(@Param("userId") Long userId, Pageable pageable);

    long countByBlogId(Long blogId);
}
