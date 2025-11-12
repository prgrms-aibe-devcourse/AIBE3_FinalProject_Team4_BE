package com.back.domain.shorlog.shorlog.controller;

import com.back.domain.shorlog.shorlog.dto.*;
import com.back.domain.shorlog.shorlog.service.ShorlogService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Shorlog", description = "숏로그 API")
@RestController
@RequestMapping("/api/v1/shorlog")
@RequiredArgsConstructor
public class ShorlogController {

    private final ShorlogService shorlogService;

    @PostMapping
    public RsData<CreateShorlogResponse> createShorlog(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateShorlogRequest request
    ) {
        return RsData.successOf(shorlogService.createShorlog(userId, request));
    }

    @GetMapping("/{id}")
    public RsData<ShorlogDetailResponse> getShorlog(@PathVariable Long id) {
        return RsData.successOf(shorlogService.getShorlog(id));
    }

    @GetMapping("/feed")
    public RsData<Page<ShorlogFeedResponse>> getFeed(
            @RequestParam(defaultValue = "0") int page
    ) {
        return RsData.successOf(shorlogService.getFeed(page));
    }

    @GetMapping("/following")
    public RsData<Page<ShorlogFeedResponse>> getFollowingFeed(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "0") int page
    ) {
        return RsData.successOf(shorlogService.getFollowingFeed(userId, page));
    }

    @GetMapping("/my")
    public RsData<Page<ShorlogFeedResponse>> getMyShorlogs(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page
    ) {
        return RsData.successOf(shorlogService.getMyShorlogs(userId, sort, page));
    }

    @PutMapping("/{id}")
    public RsData<UpdateShorlogResponse> updateShorlog(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateShorlogRequest request
    ) {
        return RsData.successOf(shorlogService.updateShorlog(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public RsData<Void> deleteShorlog(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id
    ) {
        shorlogService.deleteShorlog(userId, id);
        return new RsData<>("200-1", "숏로그가 삭제되었습니다.");
    }
}
