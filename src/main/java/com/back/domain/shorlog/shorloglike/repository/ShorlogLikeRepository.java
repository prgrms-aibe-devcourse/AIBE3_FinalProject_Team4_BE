package com.back.domain.shorlog.shorloglike.repository;

import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.domain.shorlog.shorloglike.entity.ShorlogLike;
import com.back.domain.user.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShorlogLikeRepository extends JpaRepository<ShorlogLike, ShorlogLike.ShorlogLikeId> {

    long countByShorlog(Shorlog shorlog);

    boolean existsByShorlogAndUser(Shorlog shorlog, User user);

    Optional<ShorlogLike> findByShorlogAndUser(Shorlog shorlog, User user);

    long countByUserId(Long userId);

    @EntityGraph(attributePaths = "shorlog")
    Page<ShorlogLike> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByShorlog_Id(Long shorlogId);

    // N+1 해결: 여러 숏로그의 좋아요 수를 한 번에 조회
    @Query("""
        SELECT sl.shorlog.id, COUNT(sl)
        FROM ShorlogLike sl
        WHERE sl.shorlog.id IN :shorlogIds
        GROUP BY sl.shorlog.id
        """)
    List<Object[]> countByShorlogIds(@Param("shorlogIds") List<Long> shorlogIds);
}

