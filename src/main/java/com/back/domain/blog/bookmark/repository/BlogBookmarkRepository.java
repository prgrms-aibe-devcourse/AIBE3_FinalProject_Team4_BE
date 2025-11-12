package com.back.domain.blog.bookmark.repository;

import com.back.domain.blog.bookmark.entity.BlogBookmark;
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
public interface BlogBookmarkRepository extends JpaRepository<BlogBookmark, Long> {

    // 북마크 존재 여부 확인
    boolean existsByBlogIdAndUserId(Long blogId, Long userId);

    // 북마크 조회
    Optional<BlogBookmark> findByBlogIdAndUserId(Long blogId, Long userId);

    // 사용자의 북마크 목록 조회 (Fetch Join으로 N+1 방지)
    @EntityGraph(attributePaths = {"blog", "blog.author"})
    @Query("SELECT bm FROM BlogBookmark bm " +
            "WHERE bm.user.id = :userId " +
            "ORDER BY bm.bookmarkedAt DESC")
    Page<BlogBookmark> findByUserIdWithBlog(@Param("userId") Long userId, Pageable pageable);

    // 특정 블로그들의 북마크 여부 일괄 조회 (N+1 방지)
    @Query("SELECT bm.blog.id FROM BlogBookmark bm " +
            "WHERE bm.blog.id IN :blogIds AND bm.user.id = :userId")
    Set<Long> findBookmarkedBlogIdsByUserId(@Param("blogIds") List<Long> blogIds,
                                            @Param("userId") Long userId);

    // 블로그별 북마크 수 조회 (여러 블로그를 한 번에)
    @Query("SELECT bm.blog.id, COUNT(bm) FROM BlogBookmark bm " +
            "WHERE bm.blog.id IN :blogIds " +
            "GROUP BY bm.blog.id")
    List<Object[]> countByBlogIds(@Param("blogIds") List<Long> blogIds);

    // 사용자의 북마크 수
    long countByUserId(Long userId);

    // 블로그 삭제 시 북마크도 삭제 (cascade 대신 명시적 삭제)
    void deleteByBlogId(Long blogId);


    boolean existsByBlog_IdAndUser_Id(Long blogId, Long userId);

    long deleteByBlog_IdAndUser_Id(Long postId, Long userId);

    @Query("select count(bm) from BlogBookmark bm where bm.blog.id = :blogId")
    long countBlogBookmarkBy(Long blogId);
}