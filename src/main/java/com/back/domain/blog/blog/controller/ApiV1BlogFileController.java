package com.back.domain.blog.blog.controller;

import com.back.domain.blog.blogFile.dto.BlogFileOrderUpdateRequest;
import com.back.domain.blog.blogFile.dto.BlogMediaUploadResponse;
import com.back.domain.blog.blogFile.service.BlogMediaService;
import com.back.domain.shared.image.entity.ImageType;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Tag(name = "Blog Media API", description = "블로그 Media API")
@RequestMapping("api/v1/blogs")
@RequiredArgsConstructor
public class ApiV1BlogFileController {
    private final BlogMediaService blogMediaService;

    @PostMapping("/{blogId}/media")
    @Operation(summary = "블로그 파일 업로드")
    public RsData<BlogMediaUploadResponse> uploadBlogImage(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long blogId,
            @RequestParam(value = "files", required = false) MultipartFile image,
            @RequestParam(value = "apiImageUrl", required = false) String apiImageUrl,
            @RequestParam("type") ImageType type,
            @RequestParam(value = "aspectRatios", required = false) String aspectRatios
    ) {
        BlogMediaUploadResponse dto = blogMediaService.uploadBlogMedia(user.getId(), blogId, image, apiImageUrl, type, aspectRatios);
        return RsData.of("201-1", "블로그 파일 업로드가 완료되었습니다.", dto);
    }

    @DeleteMapping("/{blogId}/media/{imageId}")
    @Operation(summary = "블로그 파일 삭제")
    public RsData<Void> deleteBlogImage(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long blogId,
            @PathVariable Long imageId
    ) {
        blogMediaService.deleteBlogMedia(user.getId(), blogId, imageId);
        return RsData.of("200-1", "블로그 파일 삭제가 완료되었습니다.", null);
    }

    @PutMapping("/{blogId}/media/order")
    @Operation(summary = "블로그 파일 순서 변경 (Drag & Drop)")
    public RsData<Void> reorderBlogFiles(
            @PathVariable Long blogId,
            @AuthenticationPrincipal SecurityUser user,
            @RequestBody @Valid BlogFileOrderUpdateRequest req
    ) {
        blogMediaService.reorderBlogFiles(user.getId(), blogId, req.imageIds());
        return RsData.of("200-1", "이미지 순서 변경이 완료되었습니다.", null);
    }
}
