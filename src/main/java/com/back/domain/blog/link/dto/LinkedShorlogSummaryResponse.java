package com.back.domain.blog.link.dto;

import com.back.domain.shorlog.shorlog.entity.Shorlog;

import java.time.LocalDateTime;

public record LinkedShorlogSummaryResponse(
        Long shorlogId,
        String comment,
        LocalDateTime modifiedAt
) {
    public LinkedShorlogSummaryResponse(Shorlog shorlog) {
        this(
                shorlog.getId(),
                shorlog.getContent(),
                shorlog.getModifiedAt()
        );
    }
}