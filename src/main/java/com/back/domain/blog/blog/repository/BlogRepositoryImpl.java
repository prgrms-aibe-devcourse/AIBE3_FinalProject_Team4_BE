package com.back.domain.blog.blog.repository;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.entity.BlogMySortType;
import com.back.domain.blog.blog.entity.BlogStatus;
import com.back.domain.blog.blog.entity.QBlog;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class BlogRepositoryImpl implements BlogRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Blog> findMyBlogs(Long userId, BlogMySortType sortType, Pageable pageable) {
        QBlog blog = QBlog.blog;

        OrderSpecifier<?> order = switch (sortType) {
            case LATEST -> blog.createdAt.desc();
            case OLDEST -> blog.createdAt.asc();
            case POPULAR -> blog.likeCount.desc();
        };

        List<Blog> content = queryFactory
                .selectFrom(blog)
                .where(
                        blog.user.id.eq(userId),
                        blog.status.eq(BlogStatus.PUBLISHED)
                )
                .orderBy(order)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(blog.count())
                .from(blog)
                .where(
                        blog.user.id.eq(userId),
                        blog.status.eq(BlogStatus.PUBLISHED)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<Blog> findByUserId(Long userId, BlogMySortType sortType, Pageable pageable) {
        QBlog blog = QBlog.blog;
        OrderSpecifier<?> order = switch (sortType) {
            case LATEST -> blog.createdAt.desc();
            case OLDEST -> blog.createdAt.asc();
            case POPULAR -> blog.likeCount.desc();
        };
        List<Blog> content = queryFactory
                .selectFrom(blog)
                .where(
                        blog.user.id.eq(userId),
                        blog.status.eq(BlogStatus.PUBLISHED)
                )
                .orderBy(order)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        Long total = queryFactory
                .select(blog.count())
                .from(blog)
                .where(
                        blog.user.id.eq(userId)
                )
                .fetchOne();
        return new PageImpl<>(content, pageable, total);
    }
}