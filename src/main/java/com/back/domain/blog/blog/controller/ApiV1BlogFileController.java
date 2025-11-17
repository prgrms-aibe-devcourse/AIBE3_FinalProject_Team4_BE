package com.back.domain.blog.blog.controller;

import com.back.domain.blog.blogFile.dto.BlogMediaUploadResponse;
import com.back.domain.blog.blogFile.service.BlogMediaService;
import com.back.domain.shared.image.entity.ImageType;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Tag(name = "Blog", description = "블로그 Media API")
@RequestMapping("api/v1/blogs")
@RequiredArgsConstructor
public class ApiV1BlogFileController {
    private final BlogMediaService blogMediaService;

    @PostMapping("/{blogId}/media")
    public RsData<BlogMediaUploadResponse> uploadBlogImage(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long blogId,
            @RequestParam("files") MultipartFile image,
            @RequestParam("type") ImageType type,
            @RequestParam(value = "aspectRatios", required = false) String aspectRatios
    ) {
        BlogMediaUploadResponse dto = blogMediaService.uploadBlogMedia(user.getId(), blogId, image, type, aspectRatios);
        return RsData.of("201-1", "블로그 이미지 업로드가 완료되었습니다.", dto);
    }
}
