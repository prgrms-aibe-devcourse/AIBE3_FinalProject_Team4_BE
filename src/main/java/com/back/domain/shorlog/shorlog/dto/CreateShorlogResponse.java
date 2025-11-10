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
    private String thumbnailUrl;
    private String thumbnailType;
    private List<String> hashtags;
    private LocalDateTime createdAt;

    public static CreateShorlogResponse from(Shorlog shorlog, List<String> hashtags) {
        return CreateShorlogResponse.builder()
                .id(shorlog.getId())
                .userId(shorlog.getUser().getId())
                .username(shorlog.getUser().getUsername())
                .profileImgUrl(shorlog.getUser().getProfileimgurl())
                .content(shorlog.getContent())
                .thumbnailUrl(shorlog.getThumbnailUrl())
                .thumbnailType(shorlog.getThumbnailType())
                .hashtags(hashtags)
                .createdAt(shorlog.getCreateDate())
                .build();
    }
}