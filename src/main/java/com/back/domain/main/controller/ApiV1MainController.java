package com.back.domain.main.controller;

import com.back.domain.main.dto.MainSummaryDto;
import com.back.domain.main.service.MainService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/main")
public class ApiV1MainController {

    private final MainService mainService;

    @GetMapping("/summary")
    public RsData<MainSummaryDto> getMainSummary(
            @AuthenticationPrincipal SecurityUser user
    ) {
        Long userId = (user != null ? user.getId() : null);
        MainSummaryDto data = mainService.getMainSummary(userId);

        return RsData.of("200-1", "메인 데이터 조회 성공", data);
    }
}
