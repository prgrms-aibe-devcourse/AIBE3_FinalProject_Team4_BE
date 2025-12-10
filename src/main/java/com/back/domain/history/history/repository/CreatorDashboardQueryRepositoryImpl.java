package com.back.domain.history.history.repository;

import com.back.domain.blog.blog.entity.QBlog;
import com.back.domain.blog.bookmark.entity.QBlogBookmark;
import com.back.domain.blog.like.entity.QBlogLike;
import com.back.domain.comments.comments.entity.CommentsTargetType;
import com.back.domain.comments.comments.entity.QComments;
import com.back.domain.history.history.dto.CreatorOverviewDto;
import com.back.domain.history.history.entity.QContentViewHistory;
import com.back.domain.main.entity.ContentType;
import com.back.domain.shorlog.shorlog.entity.QShorlog;
import com.back.domain.shorlog.shorlogbookmark.entity.QShorlogBookmark;
import com.back.domain.shorlog.shorloglike.entity.QShorlogLike;
import com.back.domain.user.follow.entity.QFollow;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class CreatorDashboardQueryRepositoryImpl implements CreatorDashboardQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public CreatorOverviewDto getOverview(Long creatorId, LocalDateTime since) {
        QContentViewHistory h = QContentViewHistory.contentViewHistory;
        QBlog blog = QBlog.blog;
        QShorlog shorlog = QShorlog.shorlog;
        QBlogLike blogLike = QBlogLike.blogLike;
        QShorlogLike shorlogLike = QShorlogLike.shorlogLike;
        QBlogBookmark blogBookmark = QBlogBookmark.blogBookmark;
        QShorlogBookmark shorlogBookmark = QShorlogBookmark.shorlogBookmark;
        QFollow follow = QFollow.follow;
        QComments comments = QComments.comments;

        // 1) 전체 조회수 (블로그 + 숏로그)
        Long blogViews = queryFactory
                .select(h.count())
                .from(h)
                .join(blog).on(
                        h.contentType.eq(ContentType.BLOG)
                                .and(h.contentId.eq(blog.id))
                )
                .where(blog.user.id.eq(creatorId))
                .fetchOne();

        Long shorlogViews = queryFactory
                .select(h.count())
                .from(h)
                .join(shorlog).on(
                        h.contentType.eq(ContentType.SHORLOG)
                                .and(h.contentId.eq(shorlog.id))
                )
                .where(shorlog.user.id.eq(creatorId))
                .fetchOne();

        long totalViews = n(blogViews) + n(shorlogViews);

        // 2) 전체 좋아요
        Long blogLikes = queryFactory
                .select(blogLike.count())
                .from(blogLike)
                .where(blogLike.blog.user.id.eq(creatorId))
                .fetchOne();

        Long shorlogLikes = queryFactory
                .select(shorlogLike.count())
                .from(shorlogLike)
                .where(shorlogLike.shorlog.user.id.eq(creatorId))
                .fetchOne();

        long totalLikes = n(blogLikes) + n(shorlogLikes);

        // 3) 전체 북마크
        Long blogBookmarks = queryFactory
                .select(blogBookmark.count())
                .from(blogBookmark)
                .where(blogBookmark.blog.user.id.eq(creatorId))
                .fetchOne();

        Long shorlogBookmarks = queryFactory
                .select(shorlogBookmark.count())
                .from(shorlogBookmark)
                .where(shorlogBookmark.shorlog.user.id.eq(creatorId))
                .fetchOne();

        long totalBookmarks = n(blogBookmarks) + n(shorlogBookmarks);

        // 4) 전체 팔로워 수
        Long followerCount = queryFactory
                .select(follow.count())
                .from(follow)
                .where(follow.toUser.id.eq(creatorId))
                .fetchOne();

        // 5) 최근 N일 좋아요
        Long recentBlogLikes = queryFactory
                .select(blogLike.count())
                .from(blogLike)
                .where(
                        blogLike.blog.user.id.eq(creatorId),
                        blogLike.likedAt.after(since)
                )
                .fetchOne();

        Long recentShorlogLikes = queryFactory
                .select(shorlogLike.count())
                .from(shorlogLike)
                .where(
                        shorlogLike.shorlog.user.id.eq(creatorId),
                        shorlogLike.createdAt.after(since)
                )
                .fetchOne();

        long recentLikes = n(recentBlogLikes) + n(recentShorlogLikes);

        // 6) 최근 N일 북마크
        Long recentBlogBookmarks = queryFactory
                .select(blogBookmark.count())
                .from(blogBookmark)
                .where(
                        blogBookmark.blog.user.id.eq(creatorId),
                        blogBookmark.bookmarkedAt.after(since)
                )
                .fetchOne();

        Long recentShorlogBookmarks = queryFactory
                .select(shorlogBookmark.count())
                .from(shorlogBookmark)
                .where(
                        shorlogBookmark.shorlog.user.id.eq(creatorId),
                        shorlogBookmark.createdAt.after(since)
                )
                .fetchOne();

        long recentBookmarks = n(recentBlogBookmarks) + n(recentShorlogBookmarks);

        // 7) 최근 N일 댓글 수 (블로그 + 숏로그)
        Long recentBlogComments = queryFactory
                .select(comments.count())
                .from(comments)
                .where(
                        comments.targetType.eq(CommentsTargetType.BLOG),
                        comments.createdAt.after(since),
                        comments.targetId.in(
                                JPAExpressions
                                        .select(blog.id)
                                        .from(blog)
                                        .where(blog.user.id.eq(creatorId))
                        )
                )
                .fetchOne();

        Long recentShorlogComments = queryFactory
                .select(comments.count())
                .from(comments)
                .where(
                        comments.targetType.eq(CommentsTargetType.SHORLOG),
                        comments.createdAt.after(since),
                        comments.targetId.in(
                                JPAExpressions
                                        .select(shorlog.id)
                                        .from(shorlog)
                                        .where(shorlog.user.id.eq(creatorId))
                        )
                )
                .fetchOne();

        long recentComments = n(recentBlogComments) + n(recentShorlogComments);

        // 8) 최근 N일 팔로워 증가
        Long recentFollowers = queryFactory
                .select(follow.count())
                .from(follow)
                .where(
                        follow.toUser.id.eq(creatorId),
                        follow.createdAt.after(since)
                )
                .fetchOne();

        return new CreatorOverviewDto(
                totalViews,
                totalLikes,
                totalBookmarks,
                n(followerCount),
                recentLikes,
                recentBookmarks,
                recentComments,
                n(recentFollowers)
        );
    }

    private long n(Long v) {
        return v == null ? 0L : v;
    }
}