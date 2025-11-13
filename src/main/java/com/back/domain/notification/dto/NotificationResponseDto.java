package com.back.domain.notification.dto;

import com.back.domain.notification.entity.Notification;
import com.back.domain.notification.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponseDto (
        Long id,
        Long senderId,
        NotificationType type,
        Long targetId,
        String message,
        LocalDateTime createdAt
) {
    public static NotificationResponseDto from(Notification n) {
        return new NotificationResponseDto(
                n.getId(),
                n.getSenderId(),
                n.getType(),
                n.getTargetId(),
                n.getMessage(),
                n.getCreatedAt()
        );
    }
}
