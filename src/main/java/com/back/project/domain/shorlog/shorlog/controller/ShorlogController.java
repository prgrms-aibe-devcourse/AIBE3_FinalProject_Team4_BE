package com.back.project.domain.shorlog.shorlog.controller;

import com.back.project.global.rsData.RsData;
import com.back.project.domain.shorlog.shorlog.dto.CreateShorlogRequest;
import com.back.project.domain.shorlog.shorlog.dto.CreateShorlogResponse;
import com.back.project.domain.shorlog.shorlog.dto.ShorlogDetailResponse;
import com.back.project.domain.shorlog.shorlog.dto.ShorlogFeedResponse;
import com.back.project.domain.shorlog.shorlog.service.ShorlogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}