package com.back.domain.shorlog.shorlogdraft.controller;

import com.back.domain.shorlog.shorlogdraft.dto.CreateDraftRequest;
import com.back.domain.shorlog.shorlogdraft.dto.DraftResponse;
import com.back.domain.shorlog.shorlogdraft.service.ShorlogDraftService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Shorlog Draft", description = "숏로그 임시저장 API")
@RestController
@RequestMapping("/api/v1/shorlog/draft")
@RequiredArgsConstructor
public class ApiV1ShorlogDraftController {

    private final ShorlogDraftService draftService;

    @PostMapping
    @Operation(summary = "숏로그 임시저장")
    public RsData<DraftResponse> createDraft(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody CreateDraftRequest request
    ) {
        return RsData.successOf(draftService.createDraft(securityUser.getId(), request));
    }

    @GetMapping
    @Operation(summary = "임시저장 목록 조회")
    public RsData<List<DraftResponse>> getDrafts(
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        return RsData.successOf(draftService.getDrafts(securityUser.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "임시저장 상세 조회")
    public RsData<DraftResponse> getDraft(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable Long id
    ) {
        return RsData.successOf(draftService.getDraft(securityUser.getId(), id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "임시저장 수정")
    public RsData<DraftResponse> updateDraft(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable Long id,
            @Valid @RequestBody CreateDraftRequest request
    ) {
        return RsData.successOf(draftService.updateDraft(securityUser.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "임시저장 삭제")
    public RsData<Void> deleteDraft(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable Long id
    ) {
        draftService.deleteDraft(securityUser.getId(), id);
        return new RsData<>("200-1", "임시저장이 삭제되었습니다.");
    }
}
