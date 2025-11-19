package com.back.domain.recommend.recommend;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T> (
        List<T> content,
        int number,         // 페이지 번호: 0 이상의 정수
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
