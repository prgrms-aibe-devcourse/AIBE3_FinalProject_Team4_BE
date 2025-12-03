package com.back.domain.user.follow.repository;

import com.back.domain.user.follow.entity.Follow;
import com.back.domain.user.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFromUserIdAndToUserId(Long fromUserId, Long toUserId);

    Optional<Follow> findByFromUserIdAndToUserId(Long fromUserId, Long toUserId);

    @Query("SELECT f.fromUser.id FROM Follow f WHERE f.toUser.id = :userId")
    List<Long> findFollowerIdsByUserId(Long userId);

    @Query("SELECT f.toUser.id FROM Follow f WHERE f.fromUser.id = :userId")
    List<Long> findFollowingIdsByUserId(Long userId);

    @Query("""
    SELECT f.fromUser
    FROM Follow f
    WHERE f.toUser.id = :userId
    """)
    List<User> findFollowersByToUserId(Long userId);

    @Query("""
    SELECT f.toUser
    FROM Follow f
    WHERE f.fromUser.id = :userId
    """)
    List<User> findFollowingsByFromUserId(Long userId);

    long countByFromUserId(Long userId);

    long countByToUserId(Long userId);

    @Query("""
    SELECT f.toUser.id, COUNT(f)
    FROM Follow f
    WHERE f.toUser.id IN :userIds
    GROUP BY f.toUser.id
    """)
    List<Object[]> findFollowerCountsByUserIds(List<Long> userIds);


    // 내가 팔로우한 유저들
    List<Follow> findByFromUser_Id(Long fromUserId);

    // 특정 유저가 팔로우한 유저들
    List<Follow> findByFromUser(User fromUser);

    // 팔로우 관계 존재 여부 (중복 제거용)
    boolean existsByFromUser_IdAndToUser_Id(Long fromUserId, Long toUserId);

}
