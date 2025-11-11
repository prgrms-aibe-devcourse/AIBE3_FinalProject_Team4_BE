package com.back.domain.comments.comments.dto;

public record CommentCreateRequestDto(
        Long postId,
        Long userId,
        String content,
        Long parentId
) {}
