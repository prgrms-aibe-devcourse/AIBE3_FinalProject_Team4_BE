package com.back.domain.message.controller;

import com.back.domain.message.dto.MessageRequestDto;
import com.back.domain.message.dto.MessageResponseDto;
import com.back.domain.message.dto.TmpMessageRequestDto;
import com.back.domain.message.entity.MessageThread;
import com.back.domain.message.repository.MessageThreadRepository;
import com.back.domain.message.service.MessageService;
import com.back.global.config.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Slf4j
@RequiredArgsConstructor
@Controller
public class MessageSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
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
    }
}
