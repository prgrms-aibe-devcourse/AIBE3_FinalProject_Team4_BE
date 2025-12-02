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
import java.util.Optional;

@Repository
public interface ShorlogRepository extends JpaRepository<Shorlog, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Shorlog s SET s.viewCount = s.viewCount + 1 WHERE s.id = :id")
    void incrementViewCount(@Param("id") Long id);

    // Fetch Join 사용
    @Query("SELECT DISTINCT s FROM Shorlog s " +
           "JOIN FETCH s.user " +
           "LEFT JOIN FETCH s.images si " +
           "LEFT JOIN FETCH si.image " +
           "WHERE s.id = :id")
    Optional<Shorlog> findByIdWithUser(@Param("id") Long id);

     // 전체 피드 조회 (최신순)
    @Query(value = "SELECT DISTINCT s FROM Shorlog s " +
           "JOIN FETCH s.user " +
           "LEFT JOIN FETCH s.images si " +
           "LEFT JOIN FETCH si.image " +
           "ORDER BY s.createdAt DESC",
           countQuery = "SELECT COUNT(DISTINCT s) FROM Shorlog s")
    Page<Shorlog> findAllByOrderByCreatedAtDesc(Pageable pageable);

     // 팔로잉 피드 조회
     // TODO: Follow 기능 구현 후 수정 (1번 주권영)
    @Query(value = "SELECT DISTINCT s FROM Shorlog s " +
           "JOIN FETCH s.user " +
           "LEFT JOIN FETCH s.images si " +
           "LEFT JOIN FETCH si.image " +
           "WHERE s.user.id IN :followingUserIds " +
           "ORDER BY s.createdAt DESC",
           countQuery = "SELECT COUNT(DISTINCT s) FROM Shorlog s WHERE s.user.id IN :followingUserIds")
    Page<Shorlog> findByFollowingUsers(@Param("followingUserIds") List<Long> followingUserIds, Pageable pageable);

    @Query(value = "SELECT DISTINCT s FROM Shorlog s " +
           "JOIN FETCH s.user " +
           "LEFT JOIN FETCH s.images si " +
           "LEFT JOIN FETCH si.image " +
           "WHERE s.user.id = :userId " +
           "ORDER BY s.createdAt DESC",
           countQuery = "SELECT COUNT(DISTINCT s) FROM Shorlog s WHERE s.user.id = :userId")
    Page<Shorlog> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Query(value = "SELECT DISTINCT s FROM Shorlog s " +
           "JOIN FETCH s.user " +
           "LEFT JOIN FETCH s.images si " +
           "LEFT JOIN FETCH si.image " +
           "WHERE s.user.id = :userId " +
           "ORDER BY s.createdAt ASC",
           countQuery = "SELECT COUNT(DISTINCT s) FROM Shorlog s WHERE s.user.id = :userId")
    Page<Shorlog> findByUserIdOrderByCreatedAtAsc(@Param("userId") Long userId, Pageable pageable);

    // 내 쇼로그 조회 (인기순 - 조회수 + 좋아요 * 2 종합 점수)
    // Step 1: ID만 조회 (인기순 정렬)
    @Query("SELECT s.id FROM Shorlog s " +
           "LEFT JOIN ShorlogLike sl ON sl.shorlog.id = s.id " +
           "WHERE s.user.id = :userId " +
           "GROUP BY s.id " +
           "ORDER BY (s.viewCount + COUNT(sl) * 2) DESC")
    List<Long> findShorlogIdsByUserIdOrderByPopularity(@Param("userId") Long userId, Pageable pageable);

    // Step 2: ID로 FETCH JOIN 조회
    @Query("SELECT DISTINCT s FROM Shorlog s " +
           "JOIN FETCH s.user " +
           "LEFT JOIN FETCH s.images si " +
           "LEFT JOIN FETCH si.image " +
           "WHERE s.id IN :ids")
    List<Shorlog> findByIdsWithFetch(@Param("ids") List<Long> ids);

    int countAllByUserId(Long userId);

    @Query("""
            SELECT s.id, s.createdAt
            FROM Shorlog s
            WHERE s.user.id = :userId
            ORDER BY s.id DESC
            """)
    Page<Object[]> findUserShorlogActivities(@Param("userId") Long userId, Pageable pageable);

    // 블로그 연결 모달용
    @Query("SELECT DISTINCT s FROM Shorlog s " +
           "LEFT JOIN FETCH s.hashtags sh " +
           "LEFT JOIN FETCH sh.hashtag " +
           "WHERE s.user.id = :userId " +
           "ORDER BY s.modifiedAt DESC")
    List<Shorlog> findRecentShorlogsByUserId(@Param("userId") Long userId);

    // creators 목록용 - 각 유저별 인기 숏로그 ID 조회
    @Query(value = """
        SELECT user_id, shorlog_id
        FROM (
            SELECT s.user_id,
                   s.id AS shorlog_id,
                   (s.view_count + COUNT(sl.user_id) * 2) AS score,
                   ROW_NUMBER() OVER (
                        PARTITION BY s.user_id
                        ORDER BY (s.view_count + COUNT(sl.user_id) * 2) DESC, s.id DESC
                   ) AS rn
            FROM shorlog s
            LEFT JOIN shorlog_like sl ON sl.shorlog_id = s.id
            WHERE s.user_id IN (:userIds)
            GROUP BY s.user_id, s.id, s.view_count
        ) t
        WHERE t.rn = 1
        """, nativeQuery = true)
    List<Object[]> findTopShorlogIdByUserIdsOrderByPopularity(@Param("userIds") List<Long> userIds);
}
