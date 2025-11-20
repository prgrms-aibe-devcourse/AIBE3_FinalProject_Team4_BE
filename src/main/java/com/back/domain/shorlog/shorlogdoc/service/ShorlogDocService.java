package com.back.domain.shorlog.shorlogdoc.service;

import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.domain.shorlog.shorlogdoc.document.ShorlogDoc;
import com.back.domain.shorlog.shorlogdoc.repository.ShorlogDocRepository;
import com.back.domain.shorlog.shorloghashtag.repository.ShorlogHashtagRepository;
import com.back.domain.shorlog.shorloglike.repository.ShorlogLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShorlogDocService {

    private final ShorlogDocRepository shorlogDocRepository;
    private final ShorlogHashtagRepository shorlogHashtagRepository;
    private final ShorlogLikeRepository shorlogLikeRepository;

    // thumbnailUrl과 User 직접 받아서 Lazy Loading 회피
    @Transactional
    public void indexShorlog(Shorlog shorlog, String thumbnailUrl, Long userId, String nickname, String profileImgUrl) {
        List<String> hashtags = shorlogHashtagRepository.findHashtagNamesByShorlogId(shorlog.getId());

        int likeCount = (int) shorlogLikeRepository.countByShorlog(shorlog);
        int viewCount = shorlog.getViewCount();

        // 인기도 점수 계산 (viewCount + likeCount * 2)
        int popularityScore = viewCount + (likeCount * 2);

        ShorlogDoc doc = ShorlogDoc.builder()
                .id(shorlog.getId().toString())
                .userId(userId)  // 직접 전달받은 값 사용
                .nickname(nickname)  // 직접 전달받은 값 사용
                .profileImgUrl(profileImgUrl)  // 직접 전달받은 값 사용
                .content(shorlog.getContent())
                .thumbnailUrl(thumbnailUrl)  // 직접 전달받은 URL 사용
                .hashtags(hashtags)
                .viewCount(viewCount)
                .likeCount(likeCount)
                .commentCount(0) // TODO: 댓글 수 조회 (4번 이해민 개발자와 협업)
                .popularityScore(popularityScore)
                .createdAt(shorlog.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant())
                .build();

        shorlogDocRepository.save(doc);
    }

    @Transactional
    public void deleteShorlog(Long shorlogId) {
        shorlogDocRepository.deleteById(shorlogId.toString());
    }

    public Page<ShorlogDoc> searchShorlogs(String keyword, String sort, int page, int size) {
        Sort sortOption = getSortOption(sort);
        Pageable pageable = PageRequest.of(page, size, sortOption);
        return shorlogDocRepository.searchByKeywordOrHashtag(keyword, pageable);
    }

    private Sort getSortOption(String sort) {
        return switch (sort) {
            case "latest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "popular" -> Sort.by(Sort.Direction.DESC, "popularityScore");
            case "views" -> Sort.by(Sort.Direction.DESC, "viewCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt"); // 기본값: 최신순
        };
    }
}




