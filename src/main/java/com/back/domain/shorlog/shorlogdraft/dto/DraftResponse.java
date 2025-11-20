package com.back.domain.shorlog.shorlogdraft.dto;

import com.back.domain.shorlog.shorlogdraft.entity.ShorlogDraft;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DraftResponse {
    private Long id;
    private String content;
    private List<String> thumbnailUrls;
    private List<String> hashtags;
    private LocalDateTime createdAt;

    public static DraftResponse of(ShorlogDraft draft, List<String> hashtags) {
        return new DraftResponse(
                draft.getId(),
                draft.getContent(),
                draft.getThumbnailUrlList() != null ? List.copyOf(draft.getThumbnailUrlList()) : List.of(),
                hashtags != null ? List.copyOf(hashtags) : List.of(),
                draft.getCreatedAt()
        );
    }
}
