package com.back.domain.message.controller;

import com.back.domain.message.dto.*;
import com.back.domain.message.entity.MessageThread;
import com.back.domain.message.repository.MessageThreadRepository;
import com.back.domain.message.service.MessageService;
import com.back.domain.message.service.MessageThreadService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class MessageSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final MessageThreadService messageThreadService;
    private final MessageThreadRepository messageThreadRepository;

//    @MessageMapping("/messages.send") // /pub/messages.send
//    public void send(@AuthenticationPrincipal SecurityUser meUser, MessageRequestDto req) {
//        MessageResponseDto saved = messageService.save(meUser.getId(), req);
//        messagingTemplate.convertAndSend("/sub/threads." + req.messageThreadId(), saved);
//    }

    @MessageMapping("/messages.send") // /pub/messages.send
    public void sendTmp(TmpMessageRequestDto req) {
        MessageRequestDto requestDto = new MessageRequestDto(req.messageThreadId(), req.content());
        MessageResponseDto saved = messageService.save(req.meId(), requestDto);

        messagingTemplate.convertAndSend("/sub/threads." + req.messageThreadId(), saved);

        MessageThread thread = messageThreadRepository.findById(req.messageThreadId())
                .orElseThrow(() -> new IllegalArgumentException("Thread not found"));

        Long u1 = thread.getUserId1();
        Long u2 = thread.getUserId2();

        messagingTemplate.convertAndSend("/sub/users." + u1, saved);
        messagingTemplate.convertAndSend("/sub/users." + u2, saved);
        log.info("[ws] send to thread: /sub/threads.{}  users: /sub/users.{} /sub/users.{} payloadThreadId={}",
                req.messageThreadId(), u1, u2, saved.messageThreadId());

    }

    @PostMapping("/message-threads/{threadId}/read")
    public RsData<ReadMessageThreadResponseDto> read(
            @RequestParam Long meId,
            @PathVariable Long threadId,
            @RequestBody(required = false) ReadMessageThreadRequestDto req
    ) {
        Long lastMessageId = (req == null) ? null : req.lastMessageId();
        return RsData.of("200", "읽음 처리 성공",
                messageThreadService.markAsRead(meId, threadId, lastMessageId));
    }

    @PostMapping("/message-threads/{threadId}/leave")
    public RsData<Void> leaveThread(@AuthenticationPrincipal SecurityUser user,
                                    @PathVariable Long threadId) {
        messageThreadService.leaveThread(user.getId(), threadId);
        return RsData.of("200", "채팅방 나가기 성공", null);
    }

}
