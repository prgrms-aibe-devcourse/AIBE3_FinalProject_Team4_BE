package com.back.domain.notification.service;

import com.back.domain.notification.dto.NotificationResponseDto;
import com.back.domain.notification.entity.Notification;
import com.back.domain.notification.entity.NotificationType;
import com.back.domain.notification.exception.NotificationErrorCase;
import com.back.domain.notification.repository.NotificationRepository;
import com.back.global.exception.ServiceException;
import com.back.global.sse.SseEmitterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SseEmitterRepository emitterRepository;

    // 알림 생성 + SSE push
    public void send(Long receiverId, Long senderId, NotificationType type,
                     Long targetId, String message) {

        Notification notification = Notification.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .type(type)
                .targetId(targetId)
                .message(message)
                .build();

        notificationRepository.save(notification);

        sendToClient(receiverId, notification);
    }

    // SSE로 알림 push
    public void sendToClient(Long userId, Notification notification) {
        emitterRepository.get(userId).ifPresent(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .id(notification.getId().toString())
                        .name("notification")
                        .data(NotificationResponseDto.from(notification))
                );
            } catch (Exception e) {
                emitterRepository.delete(userId);
            }
        });
    }

    // 알림 목록 조회
    public List<NotificationResponseDto> getNotifications(Long userId) {
        return notificationRepository
                .findByReceiverIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponseDto::from)
                .toList();
    }

    // 읽지 않은 알림 수
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByReceiverIdAndIsReadFalse(userId);
    }

    // 읽음 처리
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ServiceException(NotificationErrorCase.NOTIFICATION_NOT_FOUND));

        if (!notification.getReceiverId().equals(userId)) {
            throw new ServiceException(NotificationErrorCase.NOTIFICATION_FORBIDDEN);
        }

        notification.markAsRead();
    }
}
