package com.back.domain.shorlog.shorlogbloglink.dto;

import com.back.domain.shorlog.shorlog.entity.Shorlog;

import java.time.LocalDateTime;
import java.util.List;

public record MyShorlogSummaryResponse(
        Long id,
        String title,
        List<String> hashtags,
        LocalDateTime modifiedAt
) {
    public MyShorlogSummaryResponse(Shorlog shorlog) {
        this(
                shorlog.getId(),
                shorlog.getContent(),
                shorlog.getHashtags().stream()
                        .map(sh -> sh.getHashtag().getName())
                        .toList(),
                shorlog.getModifiedAt()
        );
    }
}


