package com.back.domain.comments.comments.event;

import com.back.domain.comments.comments.entity.CommentsTargetType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentCreatedEvent {
    private CommentsTargetType targetType;
    private Long targetId;
}

