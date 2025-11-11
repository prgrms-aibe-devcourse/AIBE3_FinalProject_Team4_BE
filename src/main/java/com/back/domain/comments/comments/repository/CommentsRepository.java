package com.back.domain.comments.comments.repository;

import com.back.domain.comments.comments.entity.Comments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentsRepository extends JpaRepository<Comments, Long> {
    List<Comments> findByPostIdAndParentIsNullOrderByCreatedAtAsc(Long postId);
}