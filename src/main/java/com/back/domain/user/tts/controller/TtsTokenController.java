package com.back.domain.user.tts.controller;

import com.back.domain.shorlog.shorlogtts.dto.TtsTokenResponse;
import com.back.domain.shorlog.shorlogtts.dto.TtsUseResponse;
import com.back.domain.user.tts.service.TtsTokenService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User TTS Token", description = "TTS 토큰 관리 API")
@RestController
@RequestMapping("/api/v1/user/tts")
@RequiredArgsConstructor
public class TtsTokenController {

    private final TtsTokenService ttsTokenService;

    @GetMapping("/token")
    @Operation(summary = "TTS 토큰 조회")
    public RsData<TtsTokenResponse> getToken(
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        return RsData.successOf(ttsTokenService.getToken(securityUser.getId()));
    }

    @PostMapping("/use")
    @Operation(summary = "TTS 토큰 차감")
    public RsData<TtsUseResponse> useToken(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam int amount
    ) {
        return RsData.successOf(ttsTokenService.useToken(securityUser.getId(), amount));
    }
}

