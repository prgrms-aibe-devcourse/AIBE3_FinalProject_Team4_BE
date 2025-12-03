package com.back.domain.main.dto;

import lombok.Builder;

@Builder
public record MainUserCardDto(
        Long id,
        String nickname,
        String bio,
        String avatarUrl,
        boolean isFollowing
) {}
