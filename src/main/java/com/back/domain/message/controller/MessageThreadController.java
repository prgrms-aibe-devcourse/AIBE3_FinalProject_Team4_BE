package com.back.domain.message.controller;

import com.back.domain.message.dto.CreateMessageThreadResponseDto;
import com.back.domain.message.dto.MessageThreadListResponseDto;
import com.back.domain.message.dto.MessageThreadResponseDto;
import com.back.domain.message.service.MessageThreadService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/message/threads")
public class MessageThreadController {
    private final MessageThreadService messageThreadService;

    @GetMapping
    public RsData<List<MessageThreadListResponseDto>> getAllThreads(@AuthenticationPrincipal SecurityUser user) {
        List<MessageThreadListResponseDto> dots = messageThreadService.getAllThreads(user.getId());
        return RsData.of("200", "메시지 스레드 목록 조회 성공", dots);
    }

    @GetMapping("/{threadId}")
    public RsData<MessageThreadResponseDto> getThread(@AuthenticationPrincipal SecurityUser meUser, @PathVariable Long threadId) {
        MessageThreadResponseDto dto = messageThreadService.getThread(meUser.getId(), threadId);
        return RsData.of("200", "메시지 스레드 조회 성공", dto);
    }

    @PostMapping
    public RsData<CreateMessageThreadResponseDto> createThread(@AuthenticationPrincipal SecurityUser meUser,
                             @RequestParam Long otherUserId) {
        CreateMessageThreadResponseDto dto = messageThreadService.createThread(meUser.getId(), otherUserId);
        return RsData.of("200", "메시지 스레드 생성 성공", dto);
    }
}
