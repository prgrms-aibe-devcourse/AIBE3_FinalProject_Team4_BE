package com.back.domain.comments.comments.dto;

import com.back.domain.comments.comments.entity.CommentsTargetType;

public record CommentCreateRequestDto(
        CommentsTargetType targetType,
        Long targetId,
        Long parentId,
        String content
) {
}
