package com.back.domain.shorlog.shorlogbookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShorlogBookmarkResponse {

    private Boolean isBookmarked;
    private Long bookmarkCount;
}

