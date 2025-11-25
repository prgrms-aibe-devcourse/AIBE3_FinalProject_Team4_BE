package com.back.domain.user.activity.dto;

import java.time.LocalDateTime;

public record UserCommentActivityDto(
        Long postId,
        LocalDateTime activityAt,
        Long commentCount
) {
}
