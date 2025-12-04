package com.back.domain.notification.service;

import com.back.domain.notification.dto.NotificationResponseDto;
import com.back.domain.notification.entity.Notification;
import com.back.domain.notification.entity.NotificationType;
import com.back.domain.notification.exception.NotificationErrorCase;
import com.back.domain.notification.repository.NotificationRepository;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.back.global.sse.SseEmitterRepository;
import com.back.global.ut.TimeUtil;
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
    private final UserRepository userRepository;   // 🔥 추가된 부분

    // 알림 생성 + SSE 전송

    public void send(Long receiverId, Long senderId,
                     NotificationType type, Long targetId,
                     String senderNickname) {

        String message = type.createMessage(senderNickname);

        Notification notification = Notification.create(
                receiverId,
                senderId,
                type,
                targetId,
                message
        );

        notificationRepository.save(notification);

        sendToClient(receiverId, notification);
    }

    // SSE PUSH

    public void sendToClient(Long userId, Notification notification) {
        emitterRepository.get(userId).ifPresent(emitter -> {

            NotificationResponseDto dto = toDto(notification);  // 🔥 sender 정보 + 시간 포함

            try {
                emitter.send(SseEmitter.event()
                        .id(notification.getId().toString())
                        .name("notification")
                        .data(dto)
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
                .map(this::toDto)
                .toList();
    }

    // 최근 10개 알림 조회

    public List<NotificationResponseDto> getRecentNotifications(Long userId) {
        return notificationRepository
                .findTop10ByReceiverIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    //  읽지 않은 개수 조회

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

    // 전체 읽음 처리

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    // Notification → DTO 변환 (확장된 정보 포함)
    private NotificationResponseDto toDto(Notification n) {

        // 보낸 사람 정보 조회
        User sender = userRepository.findById(n.getSenderId()).orElse(null);

        String senderNickname = sender != null ? sender.getNickname() : "알 수 없음";
        String profileImage = sender != null ? sender.getProfileImgUrl() : null;

        // 상대적 시간 계산
        String relativeTime = TimeUtil.toRelativeTime(n.getCreatedAt());

        return NotificationResponseDto.from(n, senderNickname, profileImage, relativeTime);
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ServiceException(NotificationErrorCase.NOTIFICATION_NOT_FOUND));

        if (!n.getReceiverId().equals(userId)) {
            throw new ServiceException(NotificationErrorCase.NOTIFICATION_FORBIDDEN);
        }

        notificationRepository.deleteById(notificationId);
    }

    @Transactional
    public void deleteAllNotifications(Long userId) {
        notificationRepository.deleteAllByReceiverId(userId);
    }
}
