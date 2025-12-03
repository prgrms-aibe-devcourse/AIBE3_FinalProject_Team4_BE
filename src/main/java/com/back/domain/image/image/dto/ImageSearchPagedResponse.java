package com.back.domain.image.image.dto;

import java.util.List;
import java.util.Optional;

import static com.back.domain.image.image.constants.GoogleImageConstants.MAX_RESULTS_LIMIT;

public record ImageSearchPagedResponse(
        String keyword,
        int number,         // 페이지 번호: 0 이상의 정수
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        List<ImageSearchContentDto> content
) {
    public static ImageSearchPagedResponse fromUnsplash(String keyword, int number, int size, UnsplashImageSearchResult result) {
        if (result == null) {
            return new ImageSearchPagedResponse(
                    keyword,
                    number,
                    size,
                    0,
                    0,
                    true,
                    true,
                    List.of()
            );
        }

        List<ImageSearchContentDto> content = Optional.ofNullable(result.results())
                .orElse(List.of())
                .stream()
                .map(ImageSearchContentDto::new)
                .toList();

        return new ImageSearchPagedResponse(
                keyword,
                number,
                content.size(),
                result.total(),
                result.totalPages(),
                number == 0,
                number >= (result.totalPages() - 1),
                content
        );
    }

    public static ImageSearchPagedResponse fromGoogle(String keyword, int number, int size, GoogleImageSearchResult result) {
        if (result == null) {
            return new ImageSearchPagedResponse(
                    keyword,
                    number,
                    size,
                    0,
                    0,
                    true,
                    true,
                    List.of()
            );
        }

        List<ImageSearchContentDto> content = Optional.ofNullable(result.items())
                .orElse(List.of())
                .stream()
                .map(ImageSearchContentDto::new)
                .toList();

        long totalElements = getTotalElements(size, result);
        int totalPages = (int) ((totalElements + size - 1) / size);

        boolean first = number == 0;               // result.queries().previousPage() == null;
        boolean last = number >= (totalPages - 1); // result.queries().nextPage() == null;

        return new ImageSearchPagedResponse(
                keyword,
                number,
                content.size(),
                totalElements,
                totalPages,
                first,
                last,
                content
        );
    }

    public static ImageSearchPagedResponse fromPixabay(String keyword, int number, int size, PixabayImageSearchResult result) {
        if (result == null) {
            return new ImageSearchPagedResponse(
                    keyword,
                    number,
                    size,
                    0,
                    0,
                    true,
                    true,
                    List.of()
            );
        }

        List<ImageSearchContentDto> content = Optional.ofNullable(result.hits())
                .orElse(List.of())
                .stream()
                .map(ImageSearchContentDto::new)
                .toList();

        long totalElements = result.totalHits();

        int totalPages = (int) ((totalElements + size - 1) / size);

        boolean first = number == 0;
        boolean last = totalPages == 0 || number >= (totalPages - 1);

        return new ImageSearchPagedResponse(
                keyword,
                number,
                content.size(),
                totalElements,
                totalPages,
                first,
                last,
                content
        );
    }

    private static long getTotalElements(int size, GoogleImageSearchResult result) {
        String totalElementsString = result.searchInformation().totalResults();
        long maxTotalElements = (long) (MAX_RESULTS_LIMIT / size) * size;
        long totalElements;

        try {
            totalElements = Long.parseLong(totalElementsString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format(
                    "Google API 데이터 오류: 'searchInformation.totalResults' 필드('%s')를 숫자로 변환할 수 없습니다.", totalElementsString),
                    e);
        }

        if (totalElements > maxTotalElements) {
            totalElements = maxTotalElements;
        }

        return totalElements;
    }
}
