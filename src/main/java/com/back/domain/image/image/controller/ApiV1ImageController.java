package com.back.domain.image.image.controller;

import com.back.domain.image.image.dto.ImageSearchPagedResponse;
import com.back.domain.image.image.service.ImageService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.back.domain.image.image.constants.GoogleImageConstants.MAX_PAGE_SIZE;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Tag(name = "Image API", description = "섬네일용 무료 이미지 API")
public class ApiV1ImageController {
    private final ImageService imageService;

    @GetMapping("/unsplash")
    @Operation(summary = "무료 이미지(Unsplash) 목록 조회")
    public RsData<ImageSearchPagedResponse> searchUnsplashImages(@RequestParam(required = false) String keyword,
                                                                 @RequestParam(defaultValue = "0") Integer number,
                                                                 @RequestParam(defaultValue = "10") Integer size) {
        number = Math.max(0, number);
        if (size < 0) size = 10;

        return RsData.successOf(imageService.getUnsplashImages(keyword, number, size));
    }

    @GetMapping("/google")
    @Operation(summary = "구글 이미지 목록 조회")
    public RsData<ImageSearchPagedResponse> searchGoogleImages(@RequestParam(required = false) String keyword,
                                                               @RequestParam(defaultValue = "0") Integer number,
                                                               @RequestParam(defaultValue = "10") Integer size) {
        number = Math.max(0, number);
        if (size < 0 || size > MAX_PAGE_SIZE) size = 10;

        return RsData.successOf(imageService.getGoogleImages(keyword, number, size));
    }
}
