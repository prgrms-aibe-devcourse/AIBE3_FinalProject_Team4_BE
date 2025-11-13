package com.back.domain.comments.comments.repository;

import com.back.domain.comments.comments.entity.Comments;
import com.back.domain.comments.comments.entity.CommentsTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentsRepository extends JpaRepository<Comments, Long> {
    List<Comments> findByTargetTypeAndTargetIdAndParentIsNullOrderByCreatedAtAsc(
            CommentsTargetType targetType,
            Long targetId
    );
    void deleteByTargetTypeAndTargetId(CommentsTargetType targetType, Long targetId);
}