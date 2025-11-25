package com.back.domain.blog.bookmark.repository;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.entity.BlogMySortType;
import com.back.domain.blog.blog.entity.BlogStatus;
import com.back.domain.blog.blog.entity.QBlog;
import com.back.domain.blog.bookmark.entity.QBlogBookmark;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BlogBookmarkQueryRepository {
    private final JPAQueryFactory queryFactory;

    public Page<Blog> findBookmarkedBlogs(Long userId, BlogMySortType sortType, Pageable pageable) {
        QBlog blog = QBlog.blog;
        QBlogBookmark bookmark = QBlogBookmark.blogBookmark;

        // 베이스 쿼리
        JPAQuery<Blog> baseQuery = queryFactory
                .select(blog)
                .from(bookmark)
                .join(bookmark.blog, blog) // ⚠ fetchJoin 빼기
                .where(
                        bookmark.user.id.eq(userId),
                        blog.status.eq(BlogStatus.PUBLISHED)
                );

        // total count
        long total = baseQuery.fetchCount();

        // 정렬
        switch (sortType) {
            case LATEST -> baseQuery.orderBy(blog.createdAt.desc(), blog.id.desc());
            case OLDEST -> baseQuery.orderBy(blog.createdAt.asc(), blog.id.asc());
            case POPULAR -> baseQuery.orderBy(
                    blog.likeCount.desc(),
                    blog.bookmarkCount.desc(),
                    blog.viewCount.desc(),
                    blog.id.desc()
            );
        }

        // 페이지네이션 + 페치
        List<Blog> content = baseQuery
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }
}