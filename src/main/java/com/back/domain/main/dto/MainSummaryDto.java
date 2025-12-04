package com.back.domain.main.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record MainSummaryDto(
        List<MainContentCardDto> popularContents,
        List<String> trendingHashtags,
        List<MainUserCardDto> recommendedUsers
) {}
