package com.back.domain.comments.comments.repository;

import com.back.domain.comments.comments.entity.Comments;
import com.back.domain.comments.comments.entity.CommentsTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentsRepository extends JpaRepository<Comments, Long> {
    List<Comments> findByTargetTypeAndTargetIdAndParentIsNullOrderByCreatedAtAsc(
            CommentsTargetType targetType,
            Long targetId
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
}