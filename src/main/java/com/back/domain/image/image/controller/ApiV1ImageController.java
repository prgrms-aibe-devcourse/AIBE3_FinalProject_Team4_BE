package com.back.domain.image.image.controller;

import com.back.domain.image.image.dto.GoogleImageItem;
import com.back.domain.image.image.dto.UnsplashPhoto;
import com.back.domain.image.image.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Tag(name = "Image API", description = "섬네일용 무료 이미지 API")
public class ApiV1ImageController {
    private final ImageService imageService;

    @GetMapping("/unsplash")
    @Operation(summary = "무료 이미지(Unsplash) 목록 조회")
    public List<UnsplashPhoto> searchUnsplashImages(@RequestParam String keyword) {
        return imageService.getUnsplashImages(keyword);
    }

    @GetMapping("/google/")
    @Operation(summary = "구글 이미지 목록 조회")
    public List<GoogleImageItem> searchGoogleImages(@RequestParam String keyword) {
        return imageService.getGoogleImages(keyword);
    }
}
