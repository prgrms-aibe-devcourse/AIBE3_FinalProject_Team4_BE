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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SseEmitterRepository emitterRepository;

    // 알림 생성 + SSE 전송
    public void send(Long receiverId, Long senderId,
                     NotificationType type, Long targetId,
                     String senderNickname) {

        String message = type.createMessage(senderNickname);

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

    // SSE Push
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

    // 전체 알림 조회
    public List<NotificationResponseDto> getNotifications(Long userId) {
        return notificationRepository
                .findByReceiverIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponseDto::from)
                .toList();
    }

    // 최근 10개 알림 조회
    public List<NotificationResponseDto> getRecentNotifications(Long userId) {
        return notificationRepository
                .findTop10ByReceiverIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponseDto::from)
                .toList();
    }

    // 읽지 않은 개수 조회
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByReceiverIdAndIsReadFalse(userId);
    }

    // 개별 알림 읽음 처리
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ServiceException(NotificationErrorCase.NOTIFICATION_NOT_FOUND));

        if (!notification.getReceiverId().equals(userId)) {
            throw new ServiceException(NotificationErrorCase.NOTIFICATION_FORBIDDEN);
        }

        notification.markAsRead();
    }

    // 전체 알림 읽음 처리 (드롭다운 열었을 때)
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }
}
