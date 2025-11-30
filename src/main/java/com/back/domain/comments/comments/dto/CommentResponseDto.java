package com.back.domain.comments.comments.dto;

import com.back.domain.comments.comments.entity.Comments;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponseDto(
        Long id,
        String content,
        Long userId,
        String nickname,
        String userProfileImgUrl,
        int likeCount,
        boolean isLiked,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        List<CommentResponseDto> children
) {
    public static CommentResponseDto fromEntity(Comments comment, Long currentUserId) {
        boolean isLiked = currentUserId != null && comment.getLikedUserIds().contains(currentUserId);

        return new CommentResponseDto(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getId(),
                comment.getUser().getNickname(),
                comment.getUser().getProfileImgUrl(),
                comment.getLikeCount(),
                isLiked,
                comment.getCreatedAt(),
                comment.getModifiedAt(),
                comment.getChildren().stream()
                        .map(child -> CommentResponseDto.fromEntity(child, currentUserId))
                        .toList()
        );
    }

    public static CommentResponseDto fromEntity(Comments comment) {
        return fromEntity(comment, null);
    }
}




