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

    public static UpdateShorlogResponse from(Shorlog shorlog, List<String> hashtags) {
        return UpdateShorlogResponse.builder()
                .id(shorlog.getId())
                .content(shorlog.getContent())
                .thumbnailUrls(shorlog.getThumbnailUrlList())
                .hashtags(hashtags)
                .updatedAt(shorlog.getModifyDate())
                .build();
    }
}

