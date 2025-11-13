package com.back.domain.image.image.dto;

import java.util.List;
import java.util.Optional;

public record ImageSearchResBody(
        int number,         // 페이지 번호: 0 이상의 정수
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        List<ImageSearchContentDto> content
) {
    public static ImageSearchResBody fromUnsplash(int number, int size, UnsplashImageSearchResult result) {
        if (result == null) {
            return new ImageSearchResBody(
                    number,
                    size,
                    0,
                    0,
                    true,
                    true,
                    List.of()
            );
        }

        return new ImageSearchResBody(
                number,
                size,
                result.total(),
                result.totalPages(),
                number == 1,
                number == result.totalPages(),
                Optional.ofNullable(result.results())
                        .orElse(List.of())
                        .stream()
                        .map(ImageSearchContentDto::new)
                        .toList()
        );
    }

    public static ImageSearchResBody fromGoogle(int number, int size, GoogleImageSearchResult result) {
        if (result == null) {
            return new ImageSearchResBody(
                    number,
                    size,
                    0,
                    0,
                    true,
                    true,
                    List.of()
            );
        }

        String totalElementsString = result.searchInformation().totalResults();
        long totalElements;
        int totalPages;

        try {
            totalElements = Long.parseLong(totalElementsString);
            totalPages = (int) ((totalElements + size - 1) / size);
        } catch (NumberFormatException e) {
            String errorMessage = String.format(
                    "Google API 데이터 오류: 'searchInformation.totalResults' 필드('%s')를 숫자로 변환할 수 없습니다.",
                    totalElementsString
            );
            throw new IllegalArgumentException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = String.format(
                    "Google API 응답 처리 중 예상치 못한 오류 발생: 'searchInformation.totalResults' = '%s'",
                    totalElementsString
            );
            throw new RuntimeException(errorMessage, e);
        }

        return new ImageSearchResBody(
                number,
                size,
                totalElements,
                totalPages,
                result.queries().previousPage() == null,
                result.queries().nextPage() == null,
                Optional.ofNullable(result.items())
                        .orElse(List.of())
                        .stream()
                        .map(ImageSearchContentDto::new)
                        .toList()
        );
    }
}
