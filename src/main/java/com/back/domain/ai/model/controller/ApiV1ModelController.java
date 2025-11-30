package com.back.domain.ai.model.controller;

import com.back.domain.ai.model.dto.ModelAvailabilityDto;
import com.back.domain.ai.model.service.ModelUsageService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ais/model")
@RequiredArgsConstructor
public class ApiV1ModelController {
    private final ModelUsageService modelUsageService;

    @GetMapping
    public RsData<List<ModelAvailabilityDto>> getModels(@AuthenticationPrincipal SecurityUser userDetails) {
        return RsData.successOf(modelUsageService.getAllModelAvailabilities(userDetails.getId()));
    }
}
