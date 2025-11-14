package com.back.domain.notification.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {


    // 알림을 받는 사람
    private Long receiverId;

    // 알림을 발생시킨 사람
    private Long senderId;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private Long targetId;  // 관련된 엔티티 ID (댓글 ID 등)

    private String message;

    private boolean isRead = false;

    public Notification(Long receiverId, Long senderId, NotificationType type,
                        Long targetId, String message) {
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.type = type;
        this.targetId = targetId;
        this.message = message;
    }

    public static Notification create(Long receiverId, Long senderId,
                                      NotificationType type, Long targetId, String message) {
        return new Notification(receiverId, senderId, type, targetId, message);
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
