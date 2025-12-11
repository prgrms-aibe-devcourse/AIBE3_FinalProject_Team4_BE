package com.back.domain.history.history.repository;

import com.back.domain.blog.blog.entity.QBlog;
import com.back.domain.blog.bookmark.entity.QBlogBookmark;
import com.back.domain.blog.like.entity.QBlogLike;
import com.back.domain.comments.comments.entity.CommentsTargetType;
import com.back.domain.comments.comments.entity.QComments;
import com.back.domain.history.history.dto.CreatorPeriodStats;
import com.back.domain.history.history.dto.CreatorTotalStats;
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
    public CreatorTotalStats getTotalStats(Long creatorId) {
        QContentViewHistory h = QContentViewHistory.contentViewHistory;
        QBlog blog = QBlog.blog;
        QShorlog shorlog = QShorlog.shorlog;
        QBlogLike blogLike = QBlogLike.blogLike;
        QShorlogLike shorlogLike = QShorlogLike.shorlogLike;
        QBlogBookmark blogBookmark = QBlogBookmark.blogBookmark;
        QShorlogBookmark shorlogBookmark = QShorlogBookmark.shorlogBookmark;
        QFollow follow = QFollow.follow;

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

        return new CreatorTotalStats(
                totalViews,
                totalLikes,
                totalBookmarks,
                n(followerCount)
        );
    }

    @Override
    public CreatorPeriodStats getPeriodStats(Long creatorId, LocalDateTime from, LocalDateTime to) {
        QContentViewHistory h = QContentViewHistory.contentViewHistory;
        QBlog blog = QBlog.blog;
        QShorlog shorlog = QShorlog.shorlog;
        QBlogLike blogLike = QBlogLike.blogLike;
        QShorlogLike shorlogLike = QShorlogLike.shorlogLike;
        QBlogBookmark blogBookmark = QBlogBookmark.blogBookmark;
        QShorlogBookmark shorlogBookmark = QShorlogBookmark.shorlogBookmark;
        QFollow follow = QFollow.follow;
        QComments comments = QComments.comments;

        // 1) 기간 조회수
        Long periodBlogViews = queryFactory
                .select(h.count())
                .from(h)
                .join(blog).on(
                        h.contentType.eq(ContentType.BLOG)
                                .and(h.contentId.eq(blog.id))
                )
                .where(
                        blog.user.id.eq(creatorId),
                        h.createdAt.between(from, to)
                )
                .fetchOne();

        Long periodShorlogViews = queryFactory
                .select(h.count())
                .from(h)
                .join(shorlog).on(
                        h.contentType.eq(ContentType.SHORLOG)
                                .and(h.contentId.eq(shorlog.id))
                )
                .where(
                        shorlog.user.id.eq(creatorId),
                        h.createdAt.between(from, to)
                )
                .fetchOne();

        long periodViews = n(periodBlogViews) + n(periodShorlogViews);

        // 2) 기간 좋아요
        Long periodBlogLikes = queryFactory
                .select(blogLike.count())
                .from(blogLike)
                .where(
                        blogLike.blog.user.id.eq(creatorId),
                        blogLike.likedAt.between(from, to)
                )
                .fetchOne();

        Long periodShorlogLikes = queryFactory
                .select(shorlogLike.count())
                .from(shorlogLike)
                .where(
                        shorlogLike.shorlog.user.id.eq(creatorId),
                        shorlogLike.createdAt.between(from, to)
                )
                .fetchOne();

        long periodLikes = n(periodBlogLikes) + n(periodShorlogLikes);

        // 3) 기간 북마크
        Long periodBlogBookmarks = queryFactory
                .select(blogBookmark.count())
                .from(blogBookmark)
                .where(
                        blogBookmark.blog.user.id.eq(creatorId),
                        blogBookmark.bookmarkedAt.between(from, to)
                )
                .fetchOne();

        Long periodShorlogBookmarks = queryFactory
                .select(shorlogBookmark.count())
                .from(shorlogBookmark)
                .where(
                        shorlogBookmark.shorlog.user.id.eq(creatorId),
                        shorlogBookmark.createdAt.between(from, to)
                )
                .fetchOne();

        long periodBookmarks = n(periodBlogBookmarks) + n(periodShorlogBookmarks);

        // 4) 기간 댓글 수
        Long periodBlogComments = queryFactory
                .select(comments.count())
                .from(comments)
                .where(
                        comments.targetType.eq(CommentsTargetType.BLOG),
                        comments.createdAt.between(from, to),
                        comments.targetId.in(
                                JPAExpressions
                                        .select(blog.id)
                                        .from(blog)
                                        .where(blog.user.id.eq(creatorId))
                        )
                )
                .fetchOne();

        Long periodShorlogComments = queryFactory
                .select(comments.count())
                .from(comments)
                .where(
                        comments.targetType.eq(CommentsTargetType.SHORLOG),
                        comments.createdAt.between(from, to),
                        comments.targetId.in(
                                JPAExpressions
                                        .select(shorlog.id)
                                        .from(shorlog)
                                        .where(shorlog.user.id.eq(creatorId))
                        )
                )
                .fetchOne();

        long periodComments = n(periodBlogComments) + n(periodShorlogComments);

        // 5) 기간 팔로워 증가
        Long periodFollowers = queryFactory
                .select(follow.count())
                .from(follow)
                .where(
                        follow.toUser.id.eq(creatorId),
                        follow.createdAt.between(from, to)
                )
                .fetchOne();

        return new CreatorPeriodStats(
                periodViews,
                periodLikes,
                periodBookmarks,
                periodComments,
                n(periodFollowers)
        );
    }

    private long n(Long v) {
        return v == null ? 0L : v;
    }
}