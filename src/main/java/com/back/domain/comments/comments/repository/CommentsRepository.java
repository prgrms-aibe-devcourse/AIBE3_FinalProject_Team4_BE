package com.back.domain.comments.comments.repository;

import com.back.domain.comments.comments.entity.Comments;
import com.back.domain.comments.comments.entity.CommentsTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentsRepository extends JpaRepository<Comments, Long> {
    List<Comments> findByTargetTypeAndTargetIdAndParentIsNullOrderByCreatedAtAsc(
            CommentsTargetType targetType,
            Long targetId
    );

    @Query("""
            SELECT DISTINCT c FROM Comments c 
            LEFT JOIN FETCH c.user 
            LEFT JOIN FETCH c.children child 
            LEFT JOIN FETCH child.user 
            WHERE c.targetType = :targetType 
              AND c.targetId = :targetId 
              AND c.parent IS NULL 
            ORDER BY c.createdAt ASC
            """)
    List<Comments> findByTargetTypeAndTargetIdWithChildrenAndUsersOrderByCreatedAtAsc(
            @Param("targetType") CommentsTargetType targetType,
            @Param("targetId") Long targetId
    );

    void deleteByTargetTypeAndTargetId(CommentsTargetType targetType, Long targetId);

    @Query("""
            select c.targetId, count(c)
            from Comments c
            where c.targetId in :targetIds
              and c.targetType = :targetType
            group by c.targetId
            """)
    List<Object[]> countByTargetIdsAndType(@Param("targetIds") List<Long> targetIds, CommentsTargetType targetType);

    // 단일 댓글 수 조회
    Long countByTargetTypeAndTargetId(CommentsTargetType targetType, Long targetId);

    @Query("""
            SELECT c.targetId, MAX(c.createdAt), COUNT(c)
            FROM Comments c
            WHERE c.user.id = :userId
              AND c.targetType = :targetType
            GROUP BY c.targetId
            ORDER BY MAX(c.id) DESC
            """)
    Page<Object[]> findUserCommentActivities(
            @Param("userId") Long userId,
            @Param("targetType") CommentsTargetType targetType,
            Pageable pageable
    );
}