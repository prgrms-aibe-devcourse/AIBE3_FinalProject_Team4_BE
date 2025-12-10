package com.back.domain.notification.dto;

import com.back.domain.comments.comments.entity.CommentsTargetType;

public record CommentLocationResponse(
        CommentsTargetType postType,
        Long postId,
        Long commentId
) {}
