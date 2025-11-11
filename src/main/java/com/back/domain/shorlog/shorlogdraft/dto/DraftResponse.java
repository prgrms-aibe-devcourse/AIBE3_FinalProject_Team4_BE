package com.back.domain.shorlog.shorlogdraft.dto;

import com.back.domain.shorlog.shorlogdraft.entity.ShorlogDraft;
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
public class DraftResponse {
    private Long id;
    private String content;
    private List<String> thumbnailUrls;
    private List<String> hashtags;
    private LocalDateTime createdAt;

    public static DraftResponse of(ShorlogDraft draft, List<String> hashtags) {
        return DraftResponse.builder()
                .id(draft.getId())
                .content(draft.getContent())
                .thumbnailUrls(draft.getThumbnailUrlList())
                .hashtags(hashtags)
                .createdAt(draft.getCreateDate())
                .build();
    }
}

