package com.back.domain.recommend.recommend;

import java.time.LocalDateTime;

public record UserCommentActivityDto(
        Long postId,
        LocalDateTime activityAt,
        Long commentCount
) {
}
