package com.back.domain.shorlog.shorlogshare.controller;

import com.back.domain.shorlog.shorlogshare.dto.SharePreviewDto;
import com.back.domain.shorlog.shorlogshare.service.ShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.NoSuchElementException;

@Tag(name = "Share", description="숏로그 공유 API (Open Graph)")
@Controller
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @GetMapping("/share/shorlog/{id}")
    @Operation(summary = "숏로그 공유 미리보기 (Open Graph 메타태그 포함)")
    public String sharePreview(@PathVariable Long id, Model model) {
        SharePreviewDto previewData = shareService.getSharePreviewData(id);

        model.addAttribute("id", previewData.getId());
        model.addAttribute("title", previewData.getTitle());
        model.addAttribute("description", previewData.getDescription());
        model.addAttribute("imageUrl", previewData.getImageUrl());
        model.addAttribute("url", previewData.getUrl());
        model.addAttribute("author", previewData.getAuthor());

        return "share-preview";
    }

    @ExceptionHandler({NoSuchElementException.class, IllegalStateException.class})
    public String handleNotFound() {
        return "error-404";
    }
}


