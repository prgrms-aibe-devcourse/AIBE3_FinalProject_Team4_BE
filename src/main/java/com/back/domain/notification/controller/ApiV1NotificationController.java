package com.back.domain.notification.controller;

import com.back.domain.comments.comments.service.CommentsService;
import com.back.domain.notification.dto.CommentLocationResponse;
import com.back.domain.notification.dto.NotificationResponseDto;
import com.back.domain.notification.service.NotificationService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import com.back.global.sse.SseEmitterRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification API", description = "알림 관련 API")
public class ApiV1NotificationController {

    private final NotificationService notificationService;
    private final SseEmitterRepository emitterRepository;
    private final CommentsService commentsService;

    @GetMapping("/stream")
    @Operation(summary = "SSE 알림 스트림 연결")
    public SseEmitter stream(@AuthenticationPrincipal SecurityUser user) {

        Long userId = user.getId();
        SseEmitter emitter = new SseEmitter(60L * 60L * 1000);

        emitterRepository.save(userId, emitter);

        emitter.onCompletion(() -> emitterRepository.delete(userId));
        emitter.onTimeout(() -> emitterRepository.delete(userId));
        emitter.onError(e -> emitterRepository.delete(userId));

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            emitterRepository.delete(userId);
        }

        return emitter;
    }

    @GetMapping
    @Operation(summary = "전체 알림 조회")
    public RsData<List<NotificationResponseDto>> list(
            @AuthenticationPrincipal SecurityUser user
    ) {
        return RsData.successOf(notificationService.getNotifications(user.getId()));
    }

    @GetMapping("/recent")
    @Operation(summary = "최근 알림 10개 조회")
    public RsData<List<NotificationResponseDto>> recent(
            @AuthenticationPrincipal SecurityUser user
    ) {
        return RsData.successOf(notificationService.getRecentNotifications(user.getId()));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "읽지 않은 알림 개수 조회")
    public RsData<Long> unreadCount(
            @AuthenticationPrincipal SecurityUser user
    ) {
        return RsData.successOf(notificationService.getUnreadCount(user.getId()));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "특정 알림 읽음 처리")
    public RsData<Void> read(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser user
    ) {
        notificationService.markAsRead(id, user.getId());
        return RsData.successOf(null);
    }

    @PostMapping("/read-all")
    @Operation(summary = "전체 알림 읽음 처리")
    public RsData<Void> readAll(
            @AuthenticationPrincipal SecurityUser user
    ) {
        notificationService.markAllAsRead(user.getId());
        return RsData.successOf(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "특정 알림 삭제")
    public RsData<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser user
    ) {
        notificationService.deleteNotification(id, user.getId());
        return RsData.successOf(null);
    }

    @DeleteMapping
    @Operation(summary = "전체 알림 삭제")
    public RsData<Void> deleteAll(
            @AuthenticationPrincipal SecurityUser user
    ) {
        notificationService.deleteAllNotifications(user.getId());
        return RsData.successOf(null);
    }

    @GetMapping("/comments/{id}/location")
    public RsData<?> getCommentLocation(@PathVariable Long id) {
        CommentLocationResponse dto = commentsService.getCommentLocation(id);
        return RsData.successOf(dto);
    }
}
