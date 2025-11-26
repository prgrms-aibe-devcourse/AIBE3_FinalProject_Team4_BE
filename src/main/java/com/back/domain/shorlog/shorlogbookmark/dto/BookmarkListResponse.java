package com.back.domain.shorlog.shorlogbookmark.dto;

import com.back.domain.shorlog.shorlog.dto.ShorlogFeedResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkListResponse {

    private List<ShorlogFeedResponse> bookmarks;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean isLast;

    public static BookmarkListResponse from(Page<ShorlogFeedResponse> page) {
        return new BookmarkListResponse(
                page.getContent(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.isLast()
        );
    }
}

