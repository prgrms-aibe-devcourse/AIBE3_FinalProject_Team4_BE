package com.back.domain.comments.comments.dto;

import com.back.domain.comments.comments.entity.Comments;

import java.util.List;

public record CommentResponseDto(
        Long id,
        String content,
        Long userId,
        String userProfileImgUrl,
        int likeCount,
        List<CommentResponseDto> children
) {
    public static CommentResponseDto fromEntity(Comments comment) {
        return new CommentResponseDto(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getId(),  // user → userId 노출
                comment.getUser().getProfileImgUrl(),
                comment.getLikeCount(),
                comment.getChildren().stream()
                        .map(CommentResponseDto::fromEntity)
                        .toList()
        );
    }
}




