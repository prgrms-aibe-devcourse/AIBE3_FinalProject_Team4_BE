package com.back.domain.notification.service;

import com.back.domain.comments.comments.entity.Comments;
import com.back.domain.comments.comments.exception.CommentsErrorCase;
import com.back.domain.comments.comments.repository.CommentsRepository;
import com.back.domain.notification.dto.CommentLocationResponse;
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
    private final UserRepository userRepository;
    private final CommentsRepository commentsRepository;

    /** ===========================
     *  알림 생성 + SSE PUSH
     * =========================== */
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

    /** ===========================
     *  SSE 실시간 전송
     * =========================== */
    public void sendToClient(Long userId, Notification notification) {
        emitterRepository.get(userId).ifPresent(emitter -> {

            NotificationResponseDto dto = toDto(notification);

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


    /** ===========================
     *  전체 알림 조회
     * =========================== */
    public List<NotificationResponseDto> getNotifications(Long userId) {
        return notificationRepository
                .findByReceiverIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    /** 최근 10개 조회 */
    public List<NotificationResponseDto> getRecentNotifications(Long userId) {
        return notificationRepository
                .findTop10ByReceiverIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByReceiverIdAndIsReadFalse(userId);
    }


    /** ===========================
     *  알림 읽음 처리
     * =========================== */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ServiceException(NotificationErrorCase.NOTIFICATION_NOT_FOUND));

        if (!n.getReceiverId().equals(userId)) {
            throw new ServiceException(NotificationErrorCase.NOTIFICATION_FORBIDDEN);
        }

        n.markAsRead();
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }


    /** ===========================
     *  Notification → DTO 변환
     * =========================== */
    private NotificationResponseDto toDto(Notification n) {

        // 🔹 보낸 유저 정보
        User sender = userRepository.findById(n.getSenderId()).orElse(null);

        String senderNickname = sender != null ? sender.getNickname() : "알 수 없음";
        String profileImage = sender != null ? sender.getProfileImgUrl() : null;

        // 🔹 상대적 시간
        String relativeTime = TimeUtil.toRelativeTime(n.getCreatedAt());

        //기본 redirect URL 생성 (Blog/Shortlog 기본 이동)
        String redirectUrl = n.getType().buildRedirectUrl(n.getTargetId());

        //  댓글 기반 알림이면 정확한 게시글/댓글 위치 조회
        if (n.getType() == NotificationType.BLOG_COMMENT ||
                n.getType() == NotificationType.COMMENT_REPLY ||
                n.getType() == NotificationType.SHORLOG_COMMENT) {

            CommentLocationResponse loc = getCommentLocation(n.getTargetId());

            if (loc.postType().equals("BLOG")) {
                redirectUrl = "/blogs/" + loc.postId() + "?focus=comment&cid=" + loc.commentId();
            } else {
                redirectUrl = "/shorlog/" + loc.postId() + "?focus=comment&cid=" + loc.commentId();
            }
        }

        return NotificationResponseDto.from(
                n,
                senderNickname,
                profileImage,
                relativeTime,
                redirectUrl
        );
    }


    /** 댓글 → 위치 정보 조회 */
    @Transactional(readOnly = true)
    public CommentLocationResponse getCommentLocation(Long commentId) {

        Comments comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new ServiceException(CommentsErrorCase.COMMENT_NOT_FOUND));

        return new CommentLocationResponse(
                comment.getTargetType(),  // BLOG or SHORLOG
                comment.getTargetId(),
                commentId
        );
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
