package com.back.domain.notification.service;

import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.comments.comments.entity.Comments;
import com.back.domain.comments.comments.entity.CommentsTargetType;
import com.back.domain.comments.comments.exception.CommentsErrorCase;
import com.back.domain.comments.comments.repository.CommentsRepository;
import com.back.domain.notification.dto.CommentLocationResponse;
import com.back.domain.notification.dto.NotificationResponseDto;
import com.back.domain.notification.entity.Notification;
import com.back.domain.notification.entity.NotificationType;
import com.back.domain.notification.exception.NotificationErrorCase;
import com.back.domain.notification.repository.NotificationRepository;
import com.back.domain.shorlog.shorlog.repository.ShorlogRepository;
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
    private final BlogRepository blogRepository;
    private final ShorlogRepository shorlogRepository;

    /** ----------------------------
     *  알림 생성 + SSE PUSH
     * ----------------------------- */
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

    /** ----------------------------
     *  SSE 실시간 전송
     * ----------------------------- */
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

    /** ----------------------------
     *  전체 알림 조회 (삭제된 대상 자동 제거)
     * ----------------------------- */
    @Transactional
    public List<NotificationResponseDto> getNotifications(Long userId) {

        List<Notification> list =
                notificationRepository.findByReceiverIdOrderByCreatedAtDesc(userId);

        List<Notification> valid = list.stream()
                .filter(this::isNotificationTargetValid)
                .toList();

        list.stream()
                .filter(n -> !isNotificationTargetValid(n))
                .forEach(n -> notificationRepository.deleteById(n.getId()));

        return valid.stream().map(this::toDto).toList();
    }

    /** 최근 10개 조회 */
    @Transactional
    public List<NotificationResponseDto> getRecentNotifications(Long userId) {

        List<Notification> list =
                notificationRepository.findTop10ByReceiverIdOrderByCreatedAtDesc(userId);

        List<Notification> valid = list.stream()
                .filter(this::isNotificationTargetValid)
                .toList();

        list.stream()
                .filter(n -> !isNotificationTargetValid(n))
                .forEach(n -> notificationRepository.deleteById(n.getId()));

        return valid.stream().map(this::toDto).toList();
    }


    /** ----------------------------
     *  알림 대상 유효성 검사
     * ----------------------------- */
    private boolean isNotificationTargetValid(Notification n) {

        return switch (n.getType()) {

            case BLOG_COMMENT, COMMENT_REPLY, SHORLOG_COMMENT, MENTION ->
                    commentsRepository.existsById(n.getTargetId());

            case BLOG_LIKE, BLOG_BOOKMARK ->
                    blogRepository.existsById(n.getTargetId());

            case SHORLOG_LIKE, SHORLOG_BOOKMARK ->
                    shorlogRepository.existsById(n.getTargetId());

            case FOLLOW ->
                    userRepository.existsById(n.getTargetId());

            default -> true;
        };
    }


    /** ----------------------------
     *  알림 읽음 처리
     * ----------------------------- */
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

    /** ----------------------------
     *  Notification → DTO 변환
     * ----------------------------- */
    private NotificationResponseDto toDto(Notification n) {

        User sender = userRepository.findById(n.getSenderId())
                .orElse(null);

        String senderNickname = sender != null ? sender.getNickname() : "알 수 없음";
        String profileImage = sender != null ? sender.getProfileImgUrl() : null;
        String relativeTime = TimeUtil.toRelativeTime(n.getCreatedAt());
        String redirectUrl = resolveRedirectUrl(n);

        return NotificationResponseDto.from(
                n,
                senderNickname,
                profileImage,
                relativeTime
        );
    }

    /** ----------------------------
     *  알림 타입별 Redirect URL 생성
     * ----------------------------- */
    private String resolveRedirectUrl(Notification n) {

        return switch (n.getType()) {

            // ----------- 블로그 -----------
            case BLOG_LIKE, BLOG_BOOKMARK ->
                    "/blogs/" + n.getTargetId();

            // ----------- 숏로그 -----------
            case SHORLOG_LIKE, SHORLOG_BOOKMARK ->
                    "/shorlog/" + n.getTargetId();

            // ----------- 댓글 기반 (댓글 → 게시글 매핑 필요)
            case BLOG_COMMENT,
                 SHORLOG_COMMENT,
                 COMMENT_REPLY,
                 MENTION -> {

                CommentLocationResponse loc = getCommentLocation(n.getTargetId());

                if (loc.postType() == CommentsTargetType.BLOG) {
                    yield "/blogs/" + loc.postId()
                            + "?focus=comment&cid=" + loc.commentId();
                }

                yield "/shorlog/" + loc.postId()
                        + "?focus=comment&cid=" + loc.commentId();
            }

            // ----------- 팔로우 -----------
            case FOLLOW ->
                    "/profile/" + n.getTargetId();
        };
    }

    /** 댓글 → 위치 정보 조회 */
    @Transactional(readOnly = true)
    public CommentLocationResponse getCommentLocation(Long commentId) {

        Comments comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new ServiceException(CommentsErrorCase.COMMENT_NOT_FOUND));

        return new CommentLocationResponse(
                comment.getTargetType(),
                comment.getTargetId(),
                commentId
        );
    }

    /** ----------------------------
     *  알림 삭제
     * ----------------------------- */
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

    public long getUnreadCount(Long userId) { return notificationRepository.countByReceiverIdAndIsReadFalse(userId); }
}




