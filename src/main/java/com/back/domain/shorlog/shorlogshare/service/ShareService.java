package com.back.domain.shorlog.shorlogshare.service;

import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.domain.shorlog.shorlog.repository.ShorlogRepository;
import com.back.domain.shorlog.shorlogshare.dto.SharePreviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShareService {

    private final ShorlogRepository shorlogRepository;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

     // 숏로그 공유 미리보기 데이터 조회
    public SharePreviewDto getSharePreviewData(Long shorlogId) {
        // N+1 문제 해결
        Shorlog shorlog = shorlogRepository.findByIdWithUser(shorlogId)
                .orElseThrow(() -> new NoSuchElementException("숏로그를 찾을 수 없습니다."));

        String title = extractTitle(shorlog);
        String description = extractDescription(shorlog);
        String imageUrl = extractImageUrl(shorlog);
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/shorlog/")
                .path(shorlogId.toString())
                .toUriString();
        String author = shorlog.getUser().getNickname();

        return SharePreviewDto.builder()
                .id(shorlogId)
                .title(title)
                .description(description)
                .imageUrl(imageUrl)
                .url(url)
                .author(author)
                .build();
    }

     // 숏로그 제목 추출 (첫 줄, 최대 50자)
    private String extractTitle(Shorlog shorlog) {
        String content = shorlog.getContent();
        if (content == null || content.isBlank()) {
            return "숏로그";
        }

        String firstLine = content.split("\n")[0].trim();
        if (firstLine.length() > 50) {
            return firstLine.substring(0, 50) + "...";
        }
        return firstLine;
    }

     // 숏로그 설명 추출 (전체 내용, 최대 100자)
    private String extractDescription(Shorlog shorlog) {
        String content = shorlog.getContent();
        if (content == null || content.isBlank()) {
            return "";
        }

        String description = content.replaceAll("\n", " ").trim();

        if (description.length() > 100) {
            return description.substring(0, 100) + "...";
        }
        return description;
    }

     // 썸네일 이미지 URL 추출 (첫 번째 이미지)
    private String extractImageUrl(Shorlog shorlog) {
        List<String> thumbnailUrls = shorlog.getThumbnailUrlList();

        if (thumbnailUrls == null || thumbnailUrls.isEmpty()) {
            throw new IllegalStateException("숏로그에 썸네일 이미지가 없습니다.");
        }

        return thumbnailUrls.get(0);
    }
}

