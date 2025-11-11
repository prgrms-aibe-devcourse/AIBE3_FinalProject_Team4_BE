package com.back.domain.comments.comments.dto;

import com.back.domain.comments.comments.entity.Comments;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CommentResponseDto(
        Long id,
        Long postId,
        Long userId,
        String content,
        LocalDateTime createdAt,
        int likeCount,
        List<CommentResponseDto> replies
) {
    public static CommentResponseDto fromEntity(Comments comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .likeCount(comment.getLikeCount())
                .replies(comment.getChildren().stream()
                        .map(CommentResponseDto::fromEntity)
                        .toList())
                .build();
    }
}




