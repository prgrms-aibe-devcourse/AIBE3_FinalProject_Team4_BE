package com.back.domain.notification.controller;

import com.back.domain.notification.dto.NotificationResponseDto;
import com.back.domain.notification.service.NotificationService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import com.back.global.sse.SseEmitterRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@Tag(name = "Notification API", description = "알림 조회 및 SSE 실시간 알림 스트림 API")
public class ApiV1NotificationController {

    private final NotificationService notificationService;
    private final SseEmitterRepository emitterRepository;

    // 🔥 SSE 연결
    @GetMapping("/stream")
    @Operation(
            summary = "SSE 실시간 알림 스트림 연결",
            description = """
                    클라이언트와 서버 간의 SSE(Server-Sent Events) 연결을 생성합니다.
                    이 연결을 통해 서버는 실시간 알림을 클라이언트로 푸시할 수 있습니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "SSE 연결 성공",
                            content = @Content(schema = @Schema(type = "string"))
                    )
            }
    )
    public SseEmitter stream(@AuthenticationPrincipal SecurityUser user) {

        Long userId = user.getId();

        // 60분 타임아웃
        SseEmitter emitter = new SseEmitter(60L * 60L * 1000L);

        emitterRepository.save(userId, emitter);

        emitter.onCompletion(() -> emitterRepository.delete(userId));
        emitter.onTimeout(() -> emitterRepository.delete(userId));
        emitter.onError((e) -> emitterRepository.delete(userId));

        // 초기 연결 응답
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected"));
        } catch (IOException e) {
            emitterRepository.delete(userId);
        }

        return emitter;
    }

    // 알림 목록 조회
    @GetMapping
    @Operation(
            summary = "알림 목록 조회",
            description = "로그인한 사용자의 모든 알림을 최신순으로 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = NotificationResponseDto.class))
                    )
            }
    )
    public RsData<List<NotificationResponseDto>> getNotifications(
            @AuthenticationPrincipal SecurityUser user
    ) {
        return RsData.successOf(notificationService.getNotifications(user.getId()));
    }

    // 읽지 않은 알림 개수
    @GetMapping("/unread-count")
    @Operation(
            summary = "읽지 않은 알림 개수 조회",
            description = "현재 로그인한 사용자의 읽지 않은 알림 개수를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(type = "integer"))
                    )
            }
    )
    public RsData<Long> getUnreadCount(
            @AuthenticationPrincipal SecurityUser user
    ) {
        return RsData.successOf(notificationService.getUnreadCount(user.getId()));
    }

    // 읽음 처리
    @PostMapping("/{id}/read")
    @Operation(
            summary = "특정 알림 읽음 처리",
            description = "지정한 알림을 읽음 상태로 변경합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "읽음 처리 성공",
                            content = @Content(schema = @Schema(type = "object"))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "알림을 찾을 수 없습니다.",
                            content = @Content(schema = @Schema(implementation = RsData.class))
                    )
            }
    )
    public RsData<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser user
    ) {
        notificationService.markAsRead(id, user.getId());
        return RsData.successOf(null);
    }
}
