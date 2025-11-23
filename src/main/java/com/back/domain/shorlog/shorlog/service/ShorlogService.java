package com.back.domain.shorlog.shorlog.service;

import com.back.domain.comments.comments.entity.CommentsTargetType;
import com.back.domain.comments.comments.service.CommentsService;
import com.back.domain.recommend.recommend.PostType;
import com.back.domain.recommend.recommend.service.RecommendService;
import com.back.domain.shared.hashtag.entity.Hashtag;
import com.back.domain.shared.hashtag.service.HashtagService;
import com.back.domain.shared.image.entity.Image;
import com.back.domain.shared.image.repository.ImageRepository;
import com.back.domain.shared.link.repository.ShorlogBlogLinkRepository;
import com.back.domain.shorlog.shorlog.dto.*;
import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.domain.shorlog.shorlog.event.ShorlogCreatedEvent;
import com.back.domain.shorlog.shorlog.event.ShorlogDeletedEvent;
import com.back.domain.shorlog.shorlog.event.ShorlogUpdatedEvent;
import com.back.domain.shorlog.shorlog.repository.ShorlogRepository;
import com.back.domain.shorlog.shorlogbookmark.repository.ShorlogBookmarkRepository;
import com.back.domain.shorlog.shorlogdoc.document.ShorlogDoc;
import com.back.domain.shorlog.shorlogdoc.service.ShorlogDocService;
import com.back.domain.shorlog.shorloghashtag.entity.ShorlogHashtag;
import com.back.domain.shorlog.shorloghashtag.repository.ShorlogHashtagRepository;
import com.back.domain.shorlog.shorlogimage.entity.ShorlogImages;
import com.back.domain.shorlog.shorlogimage.repository.ShorlogImagesRepository;
import com.back.domain.shorlog.shorloglike.repository.ShorlogLikeRepository;
import com.back.domain.user.follow.repository.FollowRepository;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ShorlogBlogLinkRepository shorlogBlogLinkRepository;
    private final HashtagService hashtagService;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final ShorlogImagesRepository shorlogImagesRepository;
    private final ShorlogDocService shorlogDocService;
    private final FollowRepository followRepository;
    private final CommentsService commentsService;
    private final ApplicationEventPublisher eventPublisher;
    private final RecommendService recommendService;

    private static final int MAX_HASHTAGS = 10;
    private static final int FEED_PAGE_SIZE = 30;
    private static final int SEARCH_PAGE_SIZE = 30;

    @Transactional
    public CreateShorlogResponse createShorlog(Long userId, CreateShorlogRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        Shorlog shorlog = Shorlog.create(user, request.getContent());

        Shorlog savedShorlog = shorlogRepository.save(shorlog);

        // 이미지 연결 (중복 제거)
        if (request.getImageIds() != null && !request.getImageIds().isEmpty()) {
            List<Long> uniqueImageIds = request.getImageIds().stream()
                    .distinct()
                    .toList();

            for (int i = 0; i < uniqueImageIds.size(); i++) {
                Long imageId = uniqueImageIds.get(i);

                Image image = imageRepository.findById(imageId)
                        .orElseThrow(() -> new NoSuchElementException("이미지를 찾을 수 없습니다: " + imageId));

                ShorlogImages shorlogImage = ShorlogImages.create(savedShorlog, image, i);
                shorlogImagesRepository.save(shorlogImage);

                imageRepository.incrementReferenceCount(imageId);
            }
        }

        List<String> hashtagNames = saveHashtags(savedShorlog, request.getHashtags());

        // 이미지 URL 직접 조회 (Lazy Loading 회피)
        List<String> thumbnailUrls = shorlogImagesRepository.findAllImagesByShorlogIdOrderBySort(savedShorlog.getId())
                .stream()
                .map(Image::getS3Url)
                .toList();

        eventPublisher.publishEvent(new ShorlogCreatedEvent(savedShorlog.getId()));

        return CreateShorlogResponse.of(savedShorlog, hashtagNames, thumbnailUrls);
    }

    @Transactional
    public ShorlogDetailResponse getShorlog(Long id) {
        Shorlog shorlog = shorlogRepository.findByIdWithUser(id)
                .orElseThrow(() -> new NoSuchElementException("숏로그를 찾을 수 없습니다."));

        // 조회수 증가 (Bulk Update - 영속성 컨텍스트를 거치지 않는 방식)
        shorlogRepository.incrementViewCount(id);

        // Elasticsearch 동기화 (조회수)
        shorlogDocService.updateElasticsearchCounts(id);

        List<String> hashtags = shorlogHashtagRepository.findHashtagNamesByShorlogId(id);

        // 좋아요/북마크 개수 조회
        long likeCount = shorlogLikeRepository.countByShorlog(shorlog);
        long bookmarkCount = shorlogBookmarkRepository.countByShorlog(shorlog);
        Long linkedBlogId = shorlogBlogLinkRepository.findBlogIdByShorlogId(id).orElse(null);

        // 댓글 수 조회
        Long commentCountLong = commentsService.getCommentCounts(
                List.of(id),
                CommentsTargetType.SHORLOG
        ).getOrDefault(id, 0L);
        int commentCount = commentCountLong.intValue();

        return ShorlogDetailResponse.from(shorlog, hashtags, shorlog.getViewCount() + 1,
                (int) likeCount, (int) bookmarkCount, commentCount, linkedBlogId);
    }

    public Page<ShorlogFeedResponse> getShorlogs(int page) {
        Pageable pageable = PageRequest.of(page, FEED_PAGE_SIZE);

        // 최신순 정렬
        Page<Shorlog> shorlogs = shorlogRepository.findAllByOrderByCreatedAtDesc(pageable);

        // 댓글 수 대량 조회
        List<Long> shorlogIds = shorlogs.stream()
                .map(Shorlog::getId)
                .toList();
        var commentCountMap = commentsService.getCommentCounts(shorlogIds, CommentsTargetType.SHORLOG);

        return shorlogs.map(shorlog -> {
            List<String> hashtags = shorlogHashtagRepository.findHashtagNamesByShorlogId(shorlog.getId());
            long likeCount = shorlogLikeRepository.countByShorlog(shorlog);
            int commentCount = commentCountMap.getOrDefault(shorlog.getId(), 0L).intValue();
            return ShorlogFeedResponse.from(shorlog, hashtags, (int) likeCount, commentCount);
        });
    }

    public Page<ShorlogFeedResponse> getFeed(String guestId, Long userId, int pageNumber) {
        return recommendService.getPostsOrderByRecommend(guestId, userId, pageNumber, FEED_PAGE_SIZE, PostType.SHORLOG);
    }

    public Page<ShorlogFeedResponse> getFollowingFeed(Long userId, int page) {
        List<Long> followingUserIds = followRepository.findFollowingIdsByUserId(userId);

        if (followingUserIds.isEmpty()) {
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, FEED_PAGE_SIZE);
        Page<Shorlog> shorlogs = shorlogRepository.findByFollowingUsers(followingUserIds, pageable);

        List<Long> shorlogIds = shorlogs.stream()
                .map(Shorlog::getId)
                .toList();
        var commentCountMap = commentsService.getCommentCounts(shorlogIds, CommentsTargetType.SHORLOG);

        return shorlogs.map(shorlog -> {
            List<String> hashtags = shorlogHashtagRepository.findHashtagNamesByShorlogId(shorlog.getId());
            long likeCount = shorlogLikeRepository.countByShorlog(shorlog);
            int commentCount = commentCountMap.getOrDefault(shorlog.getId(), 0L).intValue();
            return ShorlogFeedResponse.from(shorlog, hashtags, (int) likeCount, commentCount);
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

        List<Long> shorlogIds = shorlogs.stream()
                .map(Shorlog::getId)
                .toList();
        var commentCountMap = commentsService.getCommentCounts(shorlogIds, CommentsTargetType.SHORLOG);

        return shorlogs.map(shorlog -> {
            List<String> hashtags = shorlogHashtagRepository.findHashtagNamesByShorlogId(shorlog.getId());
            long likeCount = shorlogLikeRepository.countByShorlog(shorlog);
            int commentCount = commentCountMap.getOrDefault(shorlog.getId(), 0L).intValue();
            return ShorlogFeedResponse.from(shorlog, hashtags, (int) likeCount, commentCount);
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

            // shorlogId 전달하여 Lazy Loading 회피
            shorlogImagesRepository.deleteByShorlogId(shorlogId);

            // 즉시 DB에 반영
            shorlogImagesRepository.flush();

            List<Long> uniqueImageIds = request.getImageIds().stream()
                    .distinct()
                    .toList();

            for (int i = 0; i < uniqueImageIds.size(); i++) {
                Long imageId = uniqueImageIds.get(i);

                Image image = imageRepository.findById(imageId)
                        .orElseThrow(() -> new NoSuchElementException("이미지를 찾을 수 없습니다: " + imageId));

                ShorlogImages shorlogImage = ShorlogImages.create(shorlog, image, i);
                shorlogImagesRepository.save(shorlogImage);

                imageRepository.incrementReferenceCount(imageId);
            }
        }

        shorlogHashtagRepository.deleteByShorlogId(shorlogId);
        shorlogHashtagRepository.flush();

        List<String> hashtagNames = saveHashtags(shorlog, request.getHashtags());

        // 이미지 URL 직접 조회 (Lazy Loading 회피)
        List<String> thumbnailUrls = shorlogImagesRepository.findAllImagesByShorlogIdOrderBySort(shorlogId)
                .stream()
                .map(Image::getS3Url)
                .toList();

        eventPublisher.publishEvent(new ShorlogUpdatedEvent(shorlogId));

        return UpdateShorlogResponse.of(shorlog, hashtagNames, thumbnailUrls);
    }

    @Transactional
    public void deleteShorlog(Long userId, Long shorlogId) {
        Shorlog shorlog = shorlogRepository.findById(shorlogId)
                .orElseThrow(() -> new NoSuchElementException("숏로그를 찾을 수 없습니다."));

        if (!shorlog.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        shorlogBlogLinkRepository.deleteByShorlogId(shorlogId);

        List<Image> images = shorlogImagesRepository.findAllImagesByShorlogIdOrderBySort(shorlogId);
        for (Image image : images) {
            imageRepository.decrementReferenceCount(image.getId());
        }

        eventPublisher.publishEvent(new ShorlogDeletedEvent(shorlogId));

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
                .filter(hashtag -> !shorlogHashtagRepository.existsByShorlogIdAndHashtagId(shorlog.getId(), hashtag.getId()))
                .map(hashtag -> ShorlogHashtag.create(shorlog, hashtag))
                .toList();

        if (!shorlogHashtags.isEmpty()) {
            shorlogHashtagRepository.saveAll(shorlogHashtags);
        }

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
        Page<ShorlogDoc> searchResults = shorlogDocService.searchShorlogs(processedQuery, sort, page, SEARCH_PAGE_SIZE);

        // MySQL에 존재하는 숏로그만 필터링
        List<ShorlogFeedResponse> filteredResults = searchResults.stream()
                .map(doc -> {
                    Long shorlogId = Long.parseLong(doc.getId());

                    if (!shorlogRepository.existsById(shorlogId)) {
                        eventPublisher.publishEvent(new ShorlogDeletedEvent(shorlogId));
                        return null;
                    }

                    return new ShorlogFeedResponse(
                            shorlogId,
                            doc.getThumbnailUrl(),
                            doc.getProfileImgUrl(),
                            doc.getNickname(),
                            doc.getHashtags() != null ? List.copyOf(doc.getHashtags()) : List.of(),
                            doc.getLikeCount(),
                            doc.getCommentCount(),
                            ShorlogFeedResponse.extractFirstLine(doc.getContent())
                    );
                })
                .filter(response -> response != null) // null 제거
                .toList();

        // PageImpl로 반환
        return new org.springframework.data.domain.PageImpl<>(
                filteredResults,
                searchResults.getPageable(),
                searchResults.getTotalElements()
        );
    }
}