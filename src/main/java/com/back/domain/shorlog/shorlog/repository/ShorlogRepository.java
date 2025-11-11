package com.back.domain.shorlog.shorlog.repository;

import com.back.domain.shorlog.shorlog.entity.Shorlog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShorlogRepository extends JpaRepository<Shorlog, Long> {

    @Modifying
    @Query("UPDATE Shorlog s SET s.viewCount = s.viewCount + 1 WHERE s.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Shorlog s WHERE s.id = :shorlogId AND s.user.id = :userId")
    boolean existsByIdAndUserId(@Param("shorlogId") Long shorlogId, @Param("userId") Long userId);

     // 전체 피드 조회 (최신순 - AI 추천은 나중에)
     // TODO: AI 추천 알고리즘 연동 (5번 이지연)

    Page<Shorlog> findAllByOrderByCreatedAtDesc(Pageable pageable);

     // 팔로잉 피드 조회
     // TODO: Follow 기능 구현 후 수정 (1번 주권영)
    @Query("SELECT s FROM Shorlog s WHERE s.user.id IN :followingUserIds ORDER BY s.createdAt DESC")
    Page<Shorlog> findByFollowingUsers(@Param("followingUserIds") List<Long> followingUserIds, Pageable pageable);

    Page<Shorlog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Shorlog> findByUserIdOrderByCreatedAtAsc(Long userId, Pageable pageable);

    Page<Shorlog> findByUserIdOrderByViewCountDesc(Long userId, Pageable pageable);

     // 내 숏로그 조회 (인기순 - 조회수 + 좋아요 종합 점수)
     // TODO: 좋아요 기능 추가 후 (viewCount + likeCount * 2) 정렬로 변경
    @Query("SELECT s FROM Shorlog s WHERE s.user.id = :userId ORDER BY s.viewCount DESC")
    Page<Shorlog> findByUserIdOrderByPopularity(@Param("userId") Long userId, Pageable pageable);
}
