package com.back.domain.main.service;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.blog.blogFile.repository.BlogFileRepository;
import com.back.domain.blog.bloghashtag.repository.BlogHashTagRepository;
import com.back.domain.blog.bookmark.repository.BlogBookmarkRepository;
import com.back.domain.blog.like.repository.BlogLikeRepository;
import com.back.domain.main.dto.MainContentCardDto;
import com.back.domain.main.dto.MainSummaryDto;
import com.back.domain.main.dto.MainUserCardDto;
import com.back.domain.main.entity.ContentType;
import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.domain.shorlog.shorlog.repository.ShorlogRepository;
import com.back.domain.shorlog.shorlogbookmark.repository.ShorlogBookmarkRepository;
import com.back.domain.shorlog.shorloghashtag.repository.ShorlogHashtagRepository;
import com.back.domain.shorlog.shorloglike.repository.ShorlogLikeRepository;
import com.back.domain.user.follow.entity.Follow;
import com.back.domain.user.follow.repository.FollowRepository;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class MainService {

    private final ShorlogRepository shorlogRepository;
    private final BlogRepository blogRepository;

    private final BlogFileRepository blogFileRepository;

    private final BlogHashTagRepository blogHashtagRepository;
    private final ShorlogHashtagRepository shorlogHashtagRepository;

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    private final ShorlogLikeRepository shorlogLikeRepository;
    private final ShorlogBookmarkRepository shorlogBookmarkRepository;

    private final BlogLikeRepository blogLikeRepository;
    private final BlogBookmarkRepository blogBookmarkRepository;

    private static final DateTimeFormatter DF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    /** 최상단 메인 API */
    public MainSummaryDto getMainSummary(Long loginUserId) {

        List<MainContentCardDto> baseList = findPopularContentBase();
        List<MainContentCardDto> finalList = baseList.stream()
                .map(this::bindDynamicCounts)
                .toList();

        return MainSummaryDto.builder()
                .popularContents(finalList)
                .trendingHashtags(findTrendingHashtags())
                .recommendedUsers(findRecommendedUsers(loginUserId))
                .build();
    }


    // 인기 콘텐츠(캐싱)
    /** 인기글 기본 정보만 캐싱됨 (like/bookmark/view 제외) */
    @Cacheable(cacheNames = "main:popular", key = "'popular-base'")
    public List<MainContentCardDto> findPopularContentBase() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusDays(7);

        int fetchSize = 30;
        int limit = 12;

        // Shorlog
        List<MainContentCardDto> shorlogCards =
                shorlogRepository.findPopularWithImages(from, PageRequest.of(0, fetchSize))
                        .stream()
                        .map(s -> toBaseShorlogCardDto(s, now))
                        .toList();

        // Blog
        List<MainContentCardDto> blogCards =
                blogRepository.findPopularBlogs(from, PageRequest.of(0, fetchSize))
                        .stream()
                        .map(b -> toBaseBlogCardDto(b, now))
                        .toList();

        return Stream.concat(shorlogCards.stream(), blogCards.stream())
                .sorted(Comparator.comparingDouble(MainContentCardDto::score).reversed())
                .limit(limit)
                .toList();
    }


    /** 캐싱된 DTO에 최신 좋아요/북마크/조회수 적용 */
    private MainContentCardDto bindDynamicCounts(MainContentCardDto dto) {

        long likes = 0, bookmarks = 0, views = 0;

        if (dto.type() == ContentType.SHORLOG) {
            likes = shorlogLikeRepository.countByShorlog_Id(dto.id());
            bookmarks = shorlogBookmarkRepository.countByShorlog_Id(dto.id());
            views = shorlogRepository.findViewCount(dto.id());
        } else {
            likes = blogLikeRepository.countByBlogId(dto.id());
            bookmarks = blogBookmarkRepository.countByBlogId(dto.id());
            views = blogRepository.findViewCount(dto.id());
        }

        return MainContentCardDto.builder()
                .id(dto.id())
                .type(dto.type())
                .title(dto.title())
                .excerpt(dto.excerpt())
                .thumbnailUrl(dto.thumbnailUrl())
                .authorName(dto.authorName())
                .createdAt(dto.createdAt())
                .score(dto.score())
                .likeCount(likes)
                .bookmarkCount(bookmarks)
                .viewCount(views)
                .build();
    }


    //  Base DTO 변환
    private MainContentCardDto toBaseShorlogCardDto(Shorlog s, LocalDateTime now) {

        String thumbnail = s.getThumbnailUrlList().isEmpty()
                ? null
                : s.getThumbnailUrlList().get(0);

        double score = calcScore(0, 0, s.getViewCount(),
                Duration.between(s.getCreatedAt(), now).toHours());

        return MainContentCardDto.builder()
                .id(s.getId())
                .type(ContentType.SHORLOG)
                .title(cutTitle(s.getContent()))
                .excerpt(extractExcerpt(s.getContent()))
                .thumbnailUrl(thumbnail)
                .authorName(s.getUser().getNickname())
                .createdAt(s.getCreatedAt().format(DF))
                .score(score)
                .likeCount(0)
                .bookmarkCount(0)
                .viewCount(s.getViewCount())
                .build();
    }


    private MainContentCardDto toBaseBlogCardDto(Blog b, LocalDateTime now) {

        String thumbnail = blogFileRepository
                .findThumbnailListByBlogId(b.getId())
                .stream()
                .findFirst()
                .orElse(b.getThumbnailUrl());

        double score = calcScore(0, 0, b.getViewCount(),
                Duration.between(b.getCreatedAt(), now).toHours());

        return MainContentCardDto.builder()
                .id(b.getId())
                .type(ContentType.BLOG)
                .title(b.getTitle())
                .excerpt(extractExcerpt(b.getContent()))
                .thumbnailUrl(thumbnail)
                .authorName(b.getUser().getNickname())
                .createdAt(b.getCreatedAt().format(DF))
                .score(score)
                .likeCount(0)
                .bookmarkCount(0)
                .viewCount(b.getViewCount())
                .build();
    }


    private String cutTitle(String text) {
        return text.length() > 20 ? text.substring(0, 20) : text;
    }

    private String extractExcerpt(String content) {
        if (content == null) return "";
        return content.length() > 80 ? content.substring(0, 80) + "..." : content;
    }

    private double calcScore(long likes, long bookmarks, long views, long hours) {
        return likes * 3 +
                bookmarks * 2 +
                Math.log1p(views) * 2 +
                Math.max(0, 48 - hours);
    }


    // 트렌딩 해시태그
    @Cacheable(cacheNames = "main:hashtags", key="'hashtags'")
    public List<String> findTrendingHashtags() {

        LocalDateTime from = LocalDateTime.now().minusDays(7);

        List<Object[]> blogCounts = blogHashtagRepository.countHashtagUsageSince(from);
        List<Object[]> shorlogCounts = shorlogHashtagRepository.countHashtagUsageSince(from);

        Map<String, Long> countMap = new HashMap<>();

        blogCounts.forEach(row -> countMap.merge((String) row[0], (Long) row[1], Long::sum));
        shorlogCounts.forEach(row -> countMap.merge((String) row[0], (Long) row[1], Long::sum));

        return countMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .toList();
    }


    // ============================================
    // =               추천 유저 (캐싱)            =
    // ============================================

    @Cacheable(cacheNames = "main:recommended", key="'rec-'+#loginUserId")
    public List<MainUserCardDto> findRecommendedUsers(Long loginUserId) {

        if (loginUserId == null) return fallbackRecentUsers();

        List<Follow> myFollows = followRepository.findByFromUser_Id(loginUserId);

        if (myFollows.isEmpty()) return fallbackRecentUsers();

        Map<Long, Integer> scoreMap = new HashMap<>();

        for (Follow f : myFollows) {
            User friend = f.getToUser();
            List<Follow> friendFollows = followRepository.findByFromUser(friend);

            for (Follow ff : friendFollows) {
                Long candidateId = ff.getToUser().getId();

                if (candidateId.equals(loginUserId)) continue;
                if (followRepository.existsByFromUser_IdAndToUser_Id(loginUserId, candidateId)) continue;

                scoreMap.merge(candidateId, 1, Integer::sum);
            }
        }

        List<Long> sortedIds = scoreMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .toList();

        if (sortedIds.isEmpty()) return fallbackRecentUsers();

        return userRepository.findAllById(sortedIds)
                .stream()
                .map(this::toUserCardDto)
                .toList();
    }


    private List<MainUserCardDto> fallbackRecentUsers() {
        return userRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toUserCardDto)
                .toList();
    }

    private MainUserCardDto toUserCardDto(User u) {
        return MainUserCardDto.builder()
                .id(u.getId())
                .nickname(u.getNickname())
                .bio(u.getBio())
                .avatarUrl(u.getProfileImgUrl())
                .isFollowed(false)
                .build();
    }
}
