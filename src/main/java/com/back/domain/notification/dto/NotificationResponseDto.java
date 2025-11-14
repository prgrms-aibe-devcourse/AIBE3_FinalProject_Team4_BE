package com.back.domain.notification.dto;

import com.back.domain.notification.entity.Notification;
import com.back.domain.notification.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponseDto(
        Long id,
        Long senderId,
        String senderNickname,
        String senderProfileImage,
        NotificationType type,
        Long targetId,
        String message,
        boolean isRead,
        String relativeTime,        // 🔥 추가
        LocalDateTime createdAt
) {
    public static NotificationResponseDto from(
            Notification n,
            String senderNickname,
            String senderProfileImage,
            String relativeTime
    ) {
        return new NotificationResponseDto(
                n.getId(),
                n.getSenderId(),
                senderNickname,
                senderProfileImage,
                n.getType(),
                n.getTargetId(),
                n.getMessage(),
                n.isRead(),
                relativeTime,
                n.getCreatedAt()
        );
    }
}
