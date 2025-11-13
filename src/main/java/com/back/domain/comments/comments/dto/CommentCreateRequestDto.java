package com.back.domain.comments.comments.dto;

import com.back.domain.comments.comments.entity.CommentsTargetType;

public record CommentCreateRequestDto(
        CommentsTargetType targetType,
        Long targetId,
        Long parentId,
        String content,
        Long userId
) {
    public CommentCreateRequestDto withUserId(Long userId) {
        return new CommentCreateRequestDto(targetType, targetId, parentId, content, userId);
    }
}

