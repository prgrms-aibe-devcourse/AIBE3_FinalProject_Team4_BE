package com.back.project.domain.shorlog.shorlogdraft.controller;

import com.back.project.domain.shorlog.shorlogdraft.dto.CreateDraftRequest;
import com.back.project.domain.shorlog.shorlogdraft.dto.DraftResponse;
import com.back.project.domain.shorlog.shorlogdraft.service.ShorlogDraftService;
import com.back.project.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shorlog/draft")
@RequiredArgsConstructor
public class ShorlogDraftController {

    private final ShorlogDraftService draftService;

    @PostMapping
    public ResponseEntity<RsData<DraftResponse>> createDraft(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateDraftRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RsData.successOf(draftService.createDraft(userId, request)));
    }

    @GetMapping
    public ResponseEntity<RsData<List<DraftResponse>>> getDrafts(
            @RequestAttribute("userId") Long userId
    ) {
        return ResponseEntity.ok(RsData.successOf(draftService.getDrafts(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RsData<DraftResponse>> getDraft(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(RsData.successOf(draftService.getDraft(userId, id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RsData<DraftResponse>> updateDraft(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody CreateDraftRequest request
    ) {
        return ResponseEntity.ok(RsData.successOf(draftService.updateDraft(userId, id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RsData<Void>> deleteDraft(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id
    ) {
        draftService.deleteDraft(userId, id);
        return ResponseEntity.ok(new RsData<>("200-1", "임시저장이 삭제되었습니다."));
    }
}

