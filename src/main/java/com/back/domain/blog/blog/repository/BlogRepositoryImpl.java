package com.back.domain.blog.blog.repository;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.entity.BlogMySortType;
import com.back.domain.blog.blog.entity.BlogStatus;
import com.back.domain.blog.blog.entity.QBlog;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@SuppressWarnings("unused")
public class BlogRepositoryImpl implements BlogRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Blog> findAllByUserIdAndStatusWithSort(
            Long userId,
            BlogStatus status,
            BlogMySortType sortType
    ) {
        QBlog blog = QBlog.blog;

        OrderSpecifier<?> order = switch (sortType) {
            case LATEST -> blog.createdAt.desc();
            case OLDEST -> blog.createdAt.asc();
            case POPULAR -> blog.likeCount.desc();//TODO: 인기 정렬 기준 맞출지 협의
        };

        return queryFactory
                .selectFrom(blog)
                .where(
                        blog.user.id.eq(userId),
                        blog.status.eq(status)
                )
                .orderBy(order)
                .fetch();
    }
}