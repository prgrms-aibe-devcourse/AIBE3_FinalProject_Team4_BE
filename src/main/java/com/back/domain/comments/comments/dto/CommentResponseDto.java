package com.back.domain.comments.comments.dto;

import com.back.domain.comments.comments.entity.Comments;
import com.back.domain.comments.comments.entity.CommentsTargetType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CommentResponseDto(
        Long id,
        CommentsTargetType targetType,
        Long targetId,
        Long userId,
        String content,
        LocalDateTime createdAt,
        int likeCount,
        List<CommentResponseDto> replies
) {
    public static CommentResponseDto fromEntity(Comments comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .targetId(comment.getTargetId())
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




