package com.back.domain.shorlog.shorloglike.repository;

import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.domain.shorlog.shorloglike.entity.ShorlogLike;
import com.back.domain.user.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShorlogLikeRepository extends JpaRepository<ShorlogLike, ShorlogLike.ShorlogLikeId> {

    long countByShorlog(Shorlog shorlog);

    boolean existsByShorlogAndUser(Shorlog shorlog, User user);

    Optional<ShorlogLike> findByShorlogAndUser(Shorlog shorlog, User user);

    long countAllByUserId(Long userId);

    @EntityGraph(attributePaths = "shorlog")
    Page<ShorlogLike> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByShorlog_Id(Long shorlogId);
}

