package com.back.domain.shorlog.shorlog.service;

import com.back.domain.shared.hashtag.entity.Hashtag;
import com.back.domain.shared.hashtag.service.HashtagService;
import com.back.domain.shared.image.entity.Image;
import com.back.domain.shared.image.repository.ImageRepository;
import com.back.domain.shorlog.shorlog.dto.*;
import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.domain.shorlog.shorlog.repository.ShorlogRepository;
import com.back.domain.shorlog.shorlogbookmark.repository.ShorlogBookmarkRepository;
import com.back.domain.shorlog.shorlogdoc.service.ShorlogDocService;
import com.back.domain.shorlog.shorloghashtag.entity.ShorlogHashtag;
import com.back.domain.shorlog.shorloghashtag.repository.ShorlogHashtagRepository;
import com.back.domain.shorlog.shorlogimage.entity.ShorlogImages;
import com.back.domain.shorlog.shorlogimage.repository.ShorlogImagesRepository;
import com.back.domain.shorlog.shorloglike.repository.ShorlogLikeRepository;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShorlogService {

    private final ShorlogRepository shorlogRepository;
    private final ShorlogHashtagRepository shorlogHashtagRepository;
    private final ShorlogLikeRepository shorlogLikeRepository;
    private final ShorlogBookmarkRepository shorlogBookmarkRepository;
    private final HashtagService hashtagService;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final ShorlogImagesRepository shorlogImagesRepository;
    private final ShorlogDocService shorlogDocService;

    private static final int MAX_HASHTAGS = 10;
    private static final int FEED_PAGE_SIZE = 30;
    private static final int SEARCH_PAGE_SIZE = 30;

    @Transactional
    public CreateShorlogResponse createShorlog(Long userId, CreateShorlogRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        Shorlog shorlog = Shorlog.builder()
                .user(user)
                .content(request.getContent())
                .viewCount(0)
                .build();

        Shorlog savedShorlog = shorlogRepository.save(shorlog);

        // 이미지 연결
        if (request.getImageIds() != null && !request.getImageIds().isEmpty()) {
            for (int i = 0; i < request.getImageIds().size(); i++) {
                Long imageId = request.getImageIds().get(i);

                Image image = imageRepository.findById(imageId)
                        .orElseThrow(() -> new NoSuchElementException("이미지를 찾을 수 없습니다: " + imageId));

                ShorlogImages shorlogImage = ShorlogImages.create(savedShorlog, image, i);
                shorlogImagesRepository.save(shorlogImage);

                imageRepository.incrementReferenceCount(imageId);
            }
        }

        List<String> hashtagNames = saveHashtags(savedShorlog, request.getHashtags());

        shorlogDocService.indexShorlog(savedShorlog);

        return CreateShorlogResponse.from(savedShorlog, hashtagNames);
    }

    @Transactional
    public ShorlogDetailResponse getShorlog(Long id) {
        Shorlog shorlog = shorlogRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("숏로그를 찾을 수 없습니다."));

        // 조회수 증가 (Bulk Update - 영속성 컨텍스트를 거치지 않는 방식)
        shorlogRepository.incrementViewCount(id);

        List<String> hashtags = shorlogHashtagRepository.findHashtagNamesByShorlogId(id);

        // 좋아요/북마크 개수 조회
        long likeCount = shorlogLikeRepository.countByShorlog(shorlog);
        long bookmarkCount = shorlogBookmarkRepository.countByShorlog(shorlog);

        return ShorlogDetailResponse.from(shorlog, hashtags, shorlog.getViewCount() + 1,
                (int) likeCount, (int) bookmarkCount);
    }

    public Page<ShorlogFeedResponse> getFeed(int page) {
        Pageable pageable = PageRequest.of(page, FEED_PAGE_SIZE);

        // TODO: AI 추천 알고리즘 연동 (Issue #15 - 5번 이지연)
        // 현재: 최신순 정렬
        Page<Shorlog> shorlogs = shorlogRepository.findAllByOrderByCreatedAtDesc(pageable);

        return shorlogs.map(shorlog -> {
            List<String> hashtags = shorlogHashtagRepository.findHashtagNamesByShorlogId(shorlog.getId());
            long likeCount = shorlogLikeRepository.countByShorlog(shorlog);
            return ShorlogFeedResponse.from(shorlog, hashtags, (int) likeCount);
        });
    }

    public Page<ShorlogFeedResponse> getFollowingFeed(Long userId, int page) {
        // TODO: 실제 팔로잉 목록 조회 (1번 주권영 API 연동)
        // 현재는 임시로 빈 리스트 반환
        List<Long> followingUserIds = List.of(); // 임시

        if (followingUserIds.isEmpty()) {
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, FEED_PAGE_SIZE);
        Page<Shorlog> shorlogs = shorlogRepository.findByFollowingUsers(followingUserIds, pageable);

        return shorlogs.map(shorlog -> {
            List<String> hashtags = shorlogHashtagRepository.findHashtagNamesByShorlogId(shorlog.getId());
            long likeCount = shorlogLikeRepository.countByShorlog(shorlog);
            return ShorlogFeedResponse.from(shorlog, hashtags, (int) likeCount);
        });
    }

    public Page<ShorlogFeedResponse> getMyShorlogs(Long userId, String sort, int page) {
        Pageable pageable = PageRequest.of(page, FEED_PAGE_SIZE);
        Page<Shorlog> shorlogs;

        switch (sort.toLowerCase()) {
            case "popular" -> shorlogs = shorlogRepository.findByUserIdOrderByPopularity(userId, pageable);
            case "oldest" -> shorlogs = shorlogRepository.findByUserIdOrderByCreatedAtAsc(userId, pageable);
            case "latest" -> shorlogs = shorlogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
            default -> throw new IllegalArgumentException("정렬 기준은 'popular', 'oldest', 'latest' 중 하나여야 합니다.");
        }

        return shorlogs.map(shorlog -> {
            List<String> hashtags = shorlogHashtagRepository.findHashtagNamesByShorlogId(shorlog.getId());
            long likeCount = shorlogLikeRepository.countByShorlog(shorlog);
            return ShorlogFeedResponse.from(shorlog, hashtags, (int) likeCount);
        });
    }

    @Transactional
    public UpdateShorlogResponse updateShorlog(Long userId, Long shorlogId, UpdateShorlogRequest request) {
        Shorlog shorlog = shorlogRepository.findById(shorlogId)
                .orElseThrow(() -> new NoSuchElementException("숏로그를 찾을 수 없습니다."));

        if (!shorlog.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        shorlog.update(request.getContent());

        // 이미지 업데이트
        if (request.getImageIds() != null) {
            List<Image> existingImages = shorlogImagesRepository.findAllImagesByShorlogIdOrderBySort(shorlogId);
            for (Image existingImage : existingImages) {
                imageRepository.decrementReferenceCount(existingImage.getId());
            }

            shorlogImagesRepository.deleteByShorlog(shorlog);

            for (int i = 0; i < request.getImageIds().size(); i++) {
                Long imageId = request.getImageIds().get(i);

                Image image = imageRepository.findById(imageId)
                        .orElseThrow(() -> new NoSuchElementException("이미지를 찾을 수 없습니다: " + imageId));

                ShorlogImages shorlogImage = ShorlogImages.create(shorlog, image, i);
                shorlogImagesRepository.save(shorlogImage);

                imageRepository.incrementReferenceCount(imageId);
            }
        }

        shorlogHashtagRepository.deleteByShorlogId(shorlogId);
        List<String> hashtagNames = saveHashtags(shorlog, request.getHashtags());

        shorlogDocService.indexShorlog(shorlog);

        return UpdateShorlogResponse.from(shorlog, hashtagNames);
    }

    @Transactional
    public void deleteShorlog(Long userId, Long shorlogId) {
        Shorlog shorlog = shorlogRepository.findById(shorlogId)
                .orElseThrow(() -> new NoSuchElementException("숏로그를 찾을 수 없습니다."));

        if (!shorlog.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        List<Image> images = shorlogImagesRepository.findAllImagesByShorlogIdOrderBySort(shorlogId);
        for (Image image : images) {
            imageRepository.decrementReferenceCount(image.getId());
        }

        shorlogDocService.deleteShorlog(shorlogId);

        shorlogRepository.delete(shorlog);
    }


    private List<String> saveHashtags(Shorlog shorlog, List<String> hashtagNames) {
        if (hashtagNames == null || hashtagNames.isEmpty()) {
            return List.of();
        }

        List<String> uniqueHashtags = hashtagNames.stream()
                .distinct()
                .limit(MAX_HASHTAGS)
                .toList();

        List<Hashtag> hashtags = hashtagService.findOrCreateAll(uniqueHashtags);

        List<ShorlogHashtag> shorlogHashtags = hashtags.stream()
                .map(hashtag -> ShorlogHashtag.builder()
                        .shorlog(shorlog)
                        .hashtag(hashtag)
                        .build())
                .toList();

        shorlogHashtagRepository.saveAll(shorlogHashtags);

        return hashtags.stream()
                .map(Hashtag::getName)
                .toList();
    }

    public Page<ShorlogFeedResponse> searchShorlogs(String query, String sort, int page) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어를 입력해주세요.");
        }

        // 검색어에서 # 제거
        String processedQuery = query.trim().replace("#", "");

        return shorlogDocService.searchShorlogs(processedQuery, sort, page, SEARCH_PAGE_SIZE)
                .map(doc -> ShorlogFeedResponse.builder()
                        .id(Long.parseLong(doc.getId()))
                        .thumbnailUrl(doc.getThumbnailUrl())
                        .profileImgUrl(doc.getProfileImgUrl())
                        .nickname(doc.getNickname())
                        .hashtags(doc.getHashtags())
                        .likeCount(doc.getLikeCount())
                        .commentCount(doc.getCommentCount())
                        .firstLine(ShorlogFeedResponse.extractFirstLine(doc.getContent()))
                        .build());
    }
}