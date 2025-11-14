package com.back.domain.shorlog.shorlog.dto;

import com.back.domain.shorlog.shorlog.entity.Shorlog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class CreateShorlogResponse {

    private Long id;
    private Long userId;
    private String username;
    private String profileImgUrl;
    private String content;
    private List<String> thumbnailUrls;
    private List<String> hashtags;
    private LocalDateTime createdAt;

    public static CreateShorlogResponse from(Shorlog shorlog, List<String> hashtags) {
        return CreateShorlogResponse.builder()
                .id(shorlog.getId())
                .userId(shorlog.getUser().getId())
                .username(shorlog.getUser().getUsername())
                .profileImgUrl(shorlog.getUser().getProfileImgUrl())
                .content(shorlog.getContent())
                .thumbnailUrls(shorlog.getThumbnailUrlList())
                .hashtags(hashtags)
                .createdAt(shorlog.getCreatedAt())
                .build();
    }
}