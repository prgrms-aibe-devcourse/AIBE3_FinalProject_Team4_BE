package com.back.domain.message.controller;

import com.back.domain.message.service.MessageThreadService;
import com.back.global.config.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/message/threads")
public class MessageThreadController {
    private final MessageThreadService messageThreadService;

    @GetMapping
    public void getAllThreads(@AuthenticationPrincipal SecurityUser user) {

    }

    @GetMapping("/{threadId}")
    public void getThread(@PathVariable Long threadId) {

    }

    @PostMapping
    public void createThread() {

    }
}
