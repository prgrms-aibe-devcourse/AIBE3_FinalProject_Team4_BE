package com.back.domain.shared.link.repository;

import com.back.domain.shared.link.entity.ShorlogBlogLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShorlogBlogLinkRepository extends JpaRepository<ShorlogBlogLink, Long> {

    // 쇼로그 ID로 연결된 블로그 ID 목록 조회 (다대다 지원)
    @Query("SELECT sbl.blog.id FROM ShorlogBlogLink sbl WHERE sbl.shorlog.id = :shorlogId")
    List<Long> findBlogIdsByShorlogId(@Param("shorlogId") Long shorlogId);

    // 하위 호환성을 위한 메서드 (첫 번째 연결된 블로그만 반환)
    @Query("SELECT sbl.blog.id FROM ShorlogBlogLink sbl WHERE sbl.shorlog.id = :shorlogId ORDER BY sbl.createdAt ASC LIMIT 1")
    Optional<Long> findBlogIdByShorlogId(@Param("shorlogId") Long shorlogId);

    // 블로그 ID로 연결된 쇼로그 ID 목록 조회
    @Query("SELECT sbl.shorlog.id FROM ShorlogBlogLink sbl WHERE sbl.blog.id = :blogId")
    List<Long> findShorlogIdsByBlogId(@Param("blogId") Long blogId);

    Optional<ShorlogBlogLink> findByShorlogIdAndBlogId(Long shorlogId, Long blogId);

    void deleteByShorlogId(Long shorlogId);

    void deleteByBlogId(Long blogId);

    int countByBlogId(Long blogId);

    List<ShorlogBlogLink> findByBlogId(Long blogId);
}

