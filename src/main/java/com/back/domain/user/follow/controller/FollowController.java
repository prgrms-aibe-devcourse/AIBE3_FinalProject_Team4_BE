package com.back.domain.user.follow.controller;

import com.back.domain.user.follow.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FollowController {
    private final FollowService followService;
}
