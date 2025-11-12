package com.back.domain.shorlog.shorlogdraft.controller;

import com.back.domain.shorlog.shorlogdraft.dto.CreateDraftRequest;
import com.back.domain.shorlog.shorlogdraft.dto.DraftResponse;
import com.back.domain.shorlog.shorlogdraft.service.ShorlogDraftService;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shorlog/draft")
@RequiredArgsConstructor
public class ShorlogDraftController {

    private final ShorlogDraftService draftService;

    @PostMapping
    public RsData<DraftResponse> createDraft(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateDraftRequest request
    ) {
        return RsData.successOf(draftService.createDraft(userId, request));
    }

    @GetMapping
    public RsData<List<DraftResponse>> getDrafts(
            @RequestAttribute("userId") Long userId
    ) {
        return RsData.successOf(draftService.getDrafts(userId));
    }

    @GetMapping("/{id}")
    public RsData<DraftResponse> getDraft(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id
    ) {
        return RsData.successOf(draftService.getDraft(userId, id));
    }

    @PutMapping("/{id}")
    public RsData<DraftResponse> updateDraft(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody CreateDraftRequest request
    ) {
        return RsData.successOf(draftService.updateDraft(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public RsData<Void> deleteDraft(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id
    ) {
        draftService.deleteDraft(userId, id);
        return new RsData<>("200-1", "임시저장이 삭제되었습니다.");
    }
}
