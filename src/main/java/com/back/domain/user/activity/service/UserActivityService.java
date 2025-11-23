package com.back.domain.user.activity.service;

import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.blog.bookmark.repository.BlogBookmarkRepository;
import com.back.domain.blog.like.repository.BlogLikeRepository;
import com.back.domain.comments.comments.entity.CommentsTargetType;
import com.back.domain.comments.comments.repository.CommentsRepository;
import com.back.domain.shorlog.shorlog.repository.ShorlogRepository;
import com.back.domain.shorlog.shorlogbookmark.repository.ShorlogBookmarkRepository;
import com.back.domain.shorlog.shorloglike.repository.ShorlogLikeRepository;
import com.back.domain.user.activity.type.UserActivityType;
import com.back.domain.user.activity.dto.UserCommentActivityDto;
import com.back.domain.user.activity.dto.UserActivityDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserActivityService {
    private final ShorlogLikeRepository shorlogLikeRepository;
    private final BlogLikeRepository blogLikeRepository;

    private final ShorlogBookmarkRepository shorlogBookmarkRepository;
    private final BlogBookmarkRepository blogBookmarkRepository;

    private final CommentsRepository commentsRepository;

    private final ShorlogRepository shorlogRepository;
    private final BlogRepository blogRepository;

    // 좋아요한 포스터 가져오기
    public List<UserActivityDto> getUserLikedPosts(Long userId, boolean isShorlog) {
        return getUserLikedPosts(userId, isShorlog, -1);
    }

    public List<UserActivityDto> getUserLikedPosts(Long userId, boolean isShorlog, int limit) {
        int finalLimit = getValidLimit(limit, UserActivityType.LIKE.getLimit());
        Pageable pageable = PageRequest.of(0, finalLimit);

        if (isShorlog) {
            return shorlogLikeRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).stream()
                    .map(UserActivityDto::new)
                    .toList();
        }
        return blogLikeRepository.findByUserIdWithBlog(userId, pageable).stream()
                .map(UserActivityDto::new)
                .toList();
    }

    // 북마크한 포스트 가져오기
    public List<UserActivityDto> getUserBookmarkedPosts(Long userId, boolean isShorlog) {
        return getUserBookmarkedPosts(userId, isShorlog, -1);
    }

    public List<UserActivityDto> getUserBookmarkedPosts(Long userId, boolean isShorlog, int limit) {
        int finalLimit = getValidLimit(limit, UserActivityType.LIKE.getLimit());
        Pageable pageable = PageRequest.of(0, finalLimit);

        if (isShorlog) {
            return shorlogBookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).stream()
                    .map(UserActivityDto::new)
                    .toList();
        }
        return blogBookmarkRepository.findByUserIdWithBlog(userId, pageable).stream()
                .map(UserActivityDto::new)
                .toList();
    }

    // 댓글 단 포스트 가져오기
    public List<UserCommentActivityDto> getUserCommentedPosts(Long userId, boolean isShorlog) {
        return getUserCommentedPosts(userId, isShorlog, -1);
    }

    public List<UserCommentActivityDto> getUserCommentedPosts(Long userId, boolean isShorlog, int limit) {
        int finalLimit = getValidLimit(limit, UserActivityType.COMMENT.getLimit());
        Pageable pageable = PageRequest.of(0, finalLimit);

        if (isShorlog) {
            return commentsRepository.findUserCommentActivities(userId, CommentsTargetType.SHORLOG, pageable)
                    .map(o -> new UserCommentActivityDto((Long) o[0], (LocalDateTime) o[1], (Long) o[2]))
                    .toList();
        }
        return commentsRepository.findUserCommentActivities(userId, CommentsTargetType.BLOG, pageable)
                .map(o -> new UserCommentActivityDto((Long) o[0], (LocalDateTime) o[1], (Long) o[2]))
                .toList();
    }

    // 내가 작성한 포스트 가져오기
    public List<UserActivityDto> getUserWrittenPosts(Long userId, boolean isShorlog) {
        return getUserWrittenPosts(userId, isShorlog, -1);
    }

    public List<UserActivityDto> getUserWrittenPosts(Long userId, boolean isShorlog, int limit) {
        int finalLimit = getValidLimit(limit, UserActivityType.POST.getLimit());
        Pageable pageable = PageRequest.of(0, finalLimit);

        if (isShorlog) {
            return shorlogRepository.findUserShorlogActivities(userId, pageable).stream()
                    .map(o -> new UserActivityDto((Long) o[0], (LocalDateTime) o[1]))
                    .toList();
        }
        return blogRepository.findUserBlogActivities(userId, pageable).stream()
                .map(o -> new UserActivityDto((Long) o[0], (LocalDateTime) o[1]))
                .toList();
    }

    private int getValidLimit(int limit, int defaultLimit) {
        if (limit < 1 || limit > defaultLimit) {
            return defaultLimit;
        }
        return limit;
    }
}
