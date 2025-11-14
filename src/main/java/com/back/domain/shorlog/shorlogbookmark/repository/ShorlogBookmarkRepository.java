package com.back.domain.shorlog.shorlogbookmark.repository;

import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.domain.shorlog.shorlogbookmark.entity.ShorlogBookmark;
import com.back.domain.user.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShorlogBookmarkRepository extends JpaRepository<ShorlogBookmark, ShorlogBookmark.ShorlogBookmarkId> {

    long countByShorlog(Shorlog shorlog);

    boolean existsByShorlogAndUser(Shorlog shorlog, User user);

    Optional<ShorlogBookmark> findByShorlogAndUser(Shorlog shorlog, User user);

    @Query("SELECT sb FROM ShorlogBookmark sb " +
           "JOIN FETCH sb.shorlog s " +
           "JOIN FETCH s.user " +
           "WHERE sb.user = :user " +
           "ORDER BY sb.createdAt DESC")
    Page<ShorlogBookmark> findByUserOrderByCreatedAtDesc(@Param("user") User user, Pageable pageable);
}

