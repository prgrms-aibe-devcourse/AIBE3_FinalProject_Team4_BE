package com.back.domain.shorlog.shorlogbookmark.dto;

import com.back.domain.shorlog.shorlog.dto.ShorlogFeedResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookmarkListResponse {

    private List<ShorlogFeedResponse> bookmarks;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean isLast;

    public static BookmarkListResponse from(Page<ShorlogFeedResponse> page) {
        return BookmarkListResponse.builder()
                .bookmarks(page.getContent())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .isLast(page.isLast())
                .build();
    }
}

