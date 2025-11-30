package com.back.domain.ai.model.controller;

import com.back.domain.ai.model.dto.ModelAvailabilityDto;
import com.back.domain.ai.model.service.ModelUsageService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ais/model")
@RequiredArgsConstructor
@Tag(name = "AI Model API", description = "AI Model 조회 API")
public class ApiV1ModelController {
    private final ModelUsageService modelUsageService;

    @GetMapping
    @Operation(summary = "(채팅) 사용자별 모델 사용 가능 현황 조회")
    public RsData<List<ModelAvailabilityDto>> getModels(@AuthenticationPrincipal SecurityUser userDetails) {
        return RsData.successOf(modelUsageService.getAllModelAvailabilities(userDetails.getId()));
    }
}
