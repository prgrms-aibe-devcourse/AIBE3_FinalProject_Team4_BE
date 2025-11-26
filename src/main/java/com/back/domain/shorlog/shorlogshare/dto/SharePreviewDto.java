package com.back.domain.shorlog.shorlogshare.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SharePreviewDto {
    private final Long id;
    private final String title;
    private final String description;
    private final String imageUrl;
    private final String url;
    private final String author;
}

