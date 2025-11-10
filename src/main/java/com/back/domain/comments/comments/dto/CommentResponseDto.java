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
        List<CommentResponseDto> replies
) {
    public static CommentResponseDto fromEntity(Comments comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .replies(comment.getChildren().stream()
                        .map(CommentResponseDto::fromEntity)
                        .toList())
                .build();
    }
}




