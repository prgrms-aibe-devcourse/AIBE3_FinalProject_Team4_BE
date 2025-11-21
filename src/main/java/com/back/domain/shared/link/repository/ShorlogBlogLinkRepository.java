package com.back.domain.shared.link.repository;

import com.back.domain.shared.link.entity.ShorlogBlogLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShorlogBlogLinkRepository extends JpaRepository<ShorlogBlogLink, Long> {

    // 쇼로그 ID로 연결된 블로그 ID 조회
    @Query("SELECT sbl.blog.id FROM ShorlogBlogLink sbl WHERE sbl.shorlog.id = :shorlogId")
    Optional<Long> findBlogIdByShorlogId(@Param("shorlogId") Long shorlogId);

    // 블로그 ID로 연결된 쇼로그 ID 목록 조회
    @Query("SELECT sbl.shorlog.id FROM ShorlogBlogLink sbl WHERE sbl.blog.id = :blogId")
    List<Long> findShorlogIdsByBlogId(@Param("blogId") Long blogId);

    Optional<ShorlogBlogLink> findByShorlogIdAndBlogId(Long shorlogId, Long blogId);

    void deleteByShorlogId(Long shorlogId);

    void deleteByBlogId(Long blogId);

    int countByBlogId(Long blogId);
}

