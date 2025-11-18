package com.back.domain.shorlog.shorlog.controller;

import com.back.domain.shorlog.shorlog.dto.*;
import com.back.domain.shorlog.shorlog.service.ShorlogService;
import com.back.domain.shorlog.shorlogtts.dto.TtsResponse;
import com.back.domain.shorlog.shorlogtts.service.ShorlogTtsService;
import com.back.domain.shorlog.shorlogimage.dto.UploadImageResponse;
import com.back.domain.shorlog.shorlogimage.service.ImageUploadService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Shorlog", description = "숏로그 API")
@RestController
@RequestMapping("/api/v1/shorlog")
@RequiredArgsConstructor
public class ApiV1ShorlogController {

    private final ShorlogService shorlogService;
    private final ImageUploadService imageUploadService;
    private final ShorlogTtsService shorlogTtsService;

    @PostMapping
    @Operation(summary = "숏로그 작성")
    public RsData<CreateShorlogResponse> createShorlog(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody CreateShorlogRequest request
    ) {
        return RsData.successOf(shorlogService.createShorlog(securityUser.getId(), request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "숏로그 상세 조회")
    public RsData<ShorlogDetailResponse> getShorlog(@PathVariable Long id) {
        return RsData.successOf(shorlogService.getShorlog(id));
    }

    @GetMapping("/feed")
    @Operation(summary = "숏로그 전체 피드 조회")
    public RsData<Page<ShorlogFeedResponse>> getFeed(
            @RequestParam(defaultValue = "0") int page
    ) {
        // TODO: AI 추천 알고리즘 연동 (Issue #15 - 5번 이지연)
        return RsData.successOf(shorlogService.getFeed(page));
    }

    @GetMapping("/following")
    @Operation(summary = "팔로잉 피드 조회 (최신순)")
    public RsData<Page<ShorlogFeedResponse>> getFollowingFeed(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam(defaultValue = "0") int page
    ) {
        // TODO: 팔로우 API 연동 (Issue #12 - 1번 주권영)
        return RsData.successOf(shorlogService.getFollowingFeed(securityUser.getId(), page));
    }

    @GetMapping("/my")
    @Operation(summary = "내 숏로그 조회")
    public RsData<Page<ShorlogFeedResponse>> getMyShorlogs(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page
    ) {
        return RsData.successOf(shorlogService.getMyShorlogs(securityUser.getId(), sort, page));
    }

    @PutMapping("/{id}")
    @Operation(summary = "숏로그 수정")
    public RsData<UpdateShorlogResponse> updateShorlog(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateShorlogRequest request
    ) {
        return RsData.successOf(shorlogService.updateShorlog(securityUser.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "숏로그 삭제")
    public RsData<Void> deleteShorlog(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable Long id
    ) {
        shorlogService.deleteShorlog(securityUser.getId(), id);
        return new RsData<>("200-1", "숏로그가 삭제되었습니다.");
    }

    @PostMapping("/images/batch")
    @Operation(summary = "이미지 일괄 업로드")
    public RsData<List<UploadImageResponse>> uploadImages(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "aspectRatios", required = false) List<String> aspectRatios
    ) {
        return RsData.successOf(imageUploadService.uploadImages(securityUser.getId(), files, aspectRatios));
    }

    @GetMapping("/search")
    @Operation(summary = "숏로그 검색 (내용 + 해시태그)")
    public RsData<Page<ShorlogFeedResponse>> searchShorlogs(
            @RequestParam String q,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page
    ) {
        return RsData.successOf(shorlogService.searchShorlogs(q, sort, page));
    }

    @PostMapping("/{id}/tts")
    @Operation(summary = "TTS 생성 (Google Cloud Text-to-Speech)")
    public RsData<TtsResponse> generateTts(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable Long id
    ) {
        String ttsUrl = shorlogTtsService.generateTts(id, securityUser.getId());
        return RsData.successOf(TtsResponse.of(ttsUrl));
    }

    @GetMapping("/{id}/tts")
    @Operation(summary = "TTS URL 조회")
    public RsData<TtsResponse> getTts(@PathVariable Long id) {
        String ttsUrl = shorlogTtsService.getTtsUrl(id);
        return RsData.successOf(TtsResponse.of(ttsUrl));
    }
}
