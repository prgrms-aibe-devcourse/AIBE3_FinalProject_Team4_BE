package com.back.domain.shorlog.shorlog.dto;

import com.back.domain.shorlog.shorlog.entity.Shorlog;
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
    private List<String> thumbnailUrls;
    private List<String> hashtags;
    private LocalDateTime updatedAt;

    public static UpdateShorlogResponse of(Shorlog shorlog, List<String> hashtags, List<String> thumbnailUrls) {
        return UpdateShorlogResponse.builder()
                .id(shorlog.getId())
                .content(shorlog.getContent())
                .thumbnailUrls(thumbnailUrls)
                .hashtags(hashtags)
                .updatedAt(shorlog.getModifiedAt())
                .build();
    }
}

