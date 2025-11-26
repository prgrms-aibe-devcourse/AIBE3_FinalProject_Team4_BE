package com.back.domain.shorlog.shorlog.dto;

import com.back.domain.shorlog.shorlog.entity.Shorlog;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShorlogResponse {
    private Long id;
    private String content;
    private List<String> thumbnailUrls;
    private List<String> hashtags;
    private LocalDateTime updatedAt;

    public static UpdateShorlogResponse of(Shorlog shorlog, List<String> hashtags, List<String> thumbnailUrls) {
        return new UpdateShorlogResponse(
                shorlog.getId(),
                shorlog.getContent(),
                thumbnailUrls != null ? List.copyOf(thumbnailUrls) : List.of(),
                hashtags != null ? List.copyOf(hashtags) : List.of(),
                shorlog.getModifiedAt()
        );
    }
}

