package com.back.domain.shorlog.shorlog.dto;

import com.back.domain.shorlog.shorlog.entity.Shorlog;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class CreateShorlogResponse {

    private Long id;
    private Long userId;
    private String username;
    private String profileImgUrl;
    private String content;
    private List<String> thumbnailUrls;
    private List<String> hashtags;
    private LocalDateTime createdAt;

    public static CreateShorlogResponse of(Shorlog shorlog, List<String> hashtags, List<String> thumbnailUrls) {
        return new CreateShorlogResponse(
                shorlog.getId(),
                shorlog.getUser().getId(),
                shorlog.getUser().getUsername(),
                shorlog.getUser().getProfileImgUrl(),
                shorlog.getContent(),
                thumbnailUrls != null ? List.copyOf(thumbnailUrls) : List.of(),
                hashtags != null ? List.copyOf(hashtags) : List.of(),
                shorlog.getCreatedAt()
        );
    }
}