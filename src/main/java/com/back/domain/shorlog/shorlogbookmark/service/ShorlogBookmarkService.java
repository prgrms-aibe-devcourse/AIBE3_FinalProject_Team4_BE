package com.back.domain.shorlog.shorlogbookmark.service;

import com.back.domain.comments.comments.entity.CommentsTargetType;
import com.back.domain.comments.comments.service.CommentsService;
import com.back.domain.notification.entity.NotificationType;
import com.back.domain.notification.service.NotificationService;
import com.back.domain.shorlog.shorlog.dto.ShorlogFeedResponse;
import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.domain.shorlog.shorlog.repository.ShorlogRepository;
import com.back.domain.shorlog.shorlogbookmark.dto.BookmarkListResponse;
import com.back.domain.shorlog.shorlogbookmark.dto.ShorlogBookmarkResponse;
import com.back.domain.shorlog.shorlogbookmark.entity.ShorlogBookmark;
import com.back.domain.shorlog.shorlogbookmark.repository.ShorlogBookmarkRepository;
import com.back.domain.shorlog.shorloghashtag.repository.ShorlogHashtagRepository;
import com.back.domain.shorlog.shorloglike.repository.ShorlogLikeRepository;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
public class ShorlogBookmarkService {

    private final ShorlogBookmarkRepository shorlogBookmarkRepository;
    private final ShorlogRepository shorlogRepository;
    private final UserRepository userRepository;
    private final ShorlogHashtagRepository shorlogHashtagRepository;
    private final ShorlogLikeRepository shorlogLikeRepository;
    private final NotificationService notificationService;
    private final CommentsService commentsService;

    private static final int BOOKMARK_PAGE_SIZE = 30;

    @Transactional
    public ShorlogBookmarkResponse addBookmark(Long shorlogId, Long userId) {
        Shorlog shorlog = shorlogRepository.findById(shorlogId)
                .orElseThrow(() -> new NoSuchElementException("쇼로그를 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        if (shorlogBookmarkRepository.existsByShorlogAndUser(shorlog, user)) {
            throw new DataIntegrityViolationException("이미 북마크한 쇼로그입니다.");
        }

        ShorlogBookmark bookmark = ShorlogBookmark.create(shorlog, user);

        shorlogBookmarkRepository.save(bookmark);

        long bookmarkCount = shorlogBookmarkRepository.countByShorlog(shorlog);

        notificationService.send(
                shorlog.getUser().getId(),
                userId,
                NotificationType.SHORLOG_BOOKMARK,
                shorlogId,
                user.getNickname()
        );

        return new ShorlogBookmarkResponse(true, bookmarkCount);
    }

    @Transactional
    public ShorlogBookmarkResponse removeBookmark(Long shorlogId, Long userId) {
        Shorlog shorlog = shorlogRepository.findById(shorlogId)
                .orElseThrow(() -> new NoSuchElementException("쇼로그를 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        ShorlogBookmark bookmark = shorlogBookmarkRepository.findByShorlogAndUser(shorlog, user)
                .orElseThrow(() -> new NoSuchElementException("북마크하지 않은 쇼로그입니다."));

        shorlogBookmarkRepository.delete(bookmark);

        long bookmarkCount = shorlogBookmarkRepository.countByShorlog(shorlog);

        return new ShorlogBookmarkResponse(false, bookmarkCount);
    }

    public ShorlogBookmarkResponse getBookmarkStatus(Long shorlogId, Long userId) {
        Shorlog shorlog = shorlogRepository.findById(shorlogId)
                .orElseThrow(() -> new NoSuchElementException("쇼로그를 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        boolean isBookmarked = shorlogBookmarkRepository.existsByShorlogAndUser(shorlog, user);
        long bookmarkCount = shorlogBookmarkRepository.countByShorlog(shorlog);

        return new ShorlogBookmarkResponse(isBookmarked, bookmarkCount);
    }

    public BookmarkListResponse getMyBookmarks(Long userId, String sort, int page) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        Pageable pageable = PageRequest.of(page, BOOKMARK_PAGE_SIZE);
        Page<ShorlogBookmark> bookmarkPage;

        switch (sort.toLowerCase()) {
            case "popular" -> bookmarkPage = shorlogBookmarkRepository.findByUserOrderByPopularity(user, pageable);
            case "oldest" -> bookmarkPage = shorlogBookmarkRepository.findByUserOrderByCreatedAtAsc(user, pageable);
            case "latest" -> bookmarkPage = shorlogBookmarkRepository.findByUserOrderByCreatedAtDesc(user, pageable);
            default -> throw new IllegalArgumentException("정렬 기준은 'popular', 'oldest', 'latest' 중 하나여야 합니다.");
        }

        List<Long> shorlogIds = bookmarkPage.stream()
                .map(bookmark -> bookmark.getShorlog().getId())
                .toList();
        var commentCountMap = commentsService.getCommentCounts(shorlogIds, CommentsTargetType.SHORLOG);

        Page<ShorlogFeedResponse> responsePage = bookmarkPage.map(bookmark -> {
            Shorlog shorlog = bookmark.getShorlog();
            List<String> hashtags = shorlogHashtagRepository.findHashtagNamesByShorlogId(shorlog.getId());
            long likeCount = shorlogLikeRepository.countByShorlog(shorlog);
            int commentCount = commentCountMap.getOrDefault(shorlog.getId(), 0L).intValue();
            return ShorlogFeedResponse.from(shorlog, hashtags, (int) likeCount, commentCount);
        });

        return BookmarkListResponse.from(responsePage);
    }
}
