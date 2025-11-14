package com.back.domain.shorlog.shorlogbookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShorlogBookmarkResponse {

    private Long bookmarkCount;
    private Boolean isBookmarked;
}

