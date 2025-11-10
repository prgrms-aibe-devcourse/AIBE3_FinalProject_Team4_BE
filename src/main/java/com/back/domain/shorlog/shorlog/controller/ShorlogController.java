package com.back.domain.shorlog.shorlog.controller;

import com.back.domain.shorlog.shorlog.dto.*;
import com.back.domain.shorlog.shorlog.service.ShorlogService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Shorlog", description = "쇼로그 API")
@RestController
@RequestMapping("/api/v1/shorlog")
@RequiredArgsConstructor
public class ShorlogController {

    private final ShorlogService shorlogService;

    @PostMapping
    public ResponseEntity<RsData<CreateShorlogResponse>> createShorlog(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateShorlogRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RsData.successOf(shorlogService.createShorlog(userId, request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RsData<ShorlogDetailResponse>> getShorlog(@PathVariable Long id) {
        return ResponseEntity.ok(RsData.successOf(shorlogService.getShorlog(id)));
    }

    @GetMapping("/feed")
    public ResponseEntity<RsData<Page<ShorlogFeedResponse>>> getFeed(
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(RsData.successOf(shorlogService.getFeed(page)));
    }

    @GetMapping("/following")
    public ResponseEntity<RsData<Page<ShorlogFeedResponse>>> getFollowingFeed(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(RsData.successOf(shorlogService.getFollowingFeed(userId, page)));
    }

    @GetMapping("/my")
    public ResponseEntity<RsData<Page<ShorlogFeedResponse>>> getMyShorlogs(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(RsData.successOf(shorlogService.getMyShorlogs(userId, sort, page)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RsData<UpdateShorlogResponse>> updateShorlog(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateShorlogRequest request
    ) {
        return ResponseEntity.ok(RsData.successOf(shorlogService.updateShorlog(userId, id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RsData<Void>> deleteShorlog(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id
    ) {
        shorlogService.deleteShorlog(userId, id);
        return ResponseEntity.ok(new RsData<>("200-1", "쇼로그가 삭제되었습니다."));
    }
}
