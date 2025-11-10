package com.back.project.domain.shorlog.shorlog.dto;

import com.back.project.domain.shorlog.shorlog.entity.Shorlog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateShorlogResponse {
    private Long id;
    private String content;
    private String thumbnailUrl;
    private String thumbnailType;
    private List<String> hashtags;
    private LocalDateTime updatedAt;

    public static UpdateShorlogResponse from(Shorlog shorlog, List<String> hashtags) {
        return UpdateShorlogResponse.builder()
                .id(shorlog.getId())
                .content(shorlog.getContent())
                .thumbnailUrl(shorlog.getThumbnailUrl())
                .thumbnailType(shorlog.getThumbnailType())
                .hashtags(hashtags)
                .updatedAt(shorlog.getModifyDate())
                .build();
    }
}

