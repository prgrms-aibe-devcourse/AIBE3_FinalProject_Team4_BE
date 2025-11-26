package com.back.domain.search.searchHistory.exception;

import com.back.global.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SearchHistoryErrorCase implements ErrorCase {
    NOT_FOUND_SEARCH_HISTORY(HttpStatus.NOT_FOUND, 3001, "검색 기록을 찾을 수 없습니다."),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, 3002, "권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
}
