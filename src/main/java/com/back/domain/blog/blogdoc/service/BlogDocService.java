package com.back.domain.blog.blogdoc.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.back.domain.blog.blogdoc.document.BlogDoc;
import com.back.domain.blog.blogdoc.document.BlogSortType;
import com.back.domain.blog.blogdoc.dto.BlogSearchCondition;
import com.back.domain.blog.blogdoc.dto.BlogSearchResult;
import com.back.domain.blog.blogdoc.dto.BlogSliceResponse;
import com.back.domain.blog.blogdoc.dto.BlogSummaryResponse;
import com.back.domain.blog.blogdoc.repository.BlogDocQueryRepository;
import com.back.domain.blog.bookmark.service.BlogBookmarkService;
import com.back.domain.blog.like.service.BlogLikeService;
import com.back.domain.comments.comments.entity.CommentsTargetType;
import com.back.domain.comments.comments.service.CommentsService;
import com.back.domain.recommend.recommend.PostType;
import com.back.domain.recommend.recommend.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BlogDocService {
    private final BlogDocQueryRepository blogDocQueryRepository;
    private final BlogLikeService blogLikeService;
    private final BlogBookmarkService blogBookmarkService;
    private final CommentsService commentsService;
    private final RecommendService recommendService;

    public BlogSliceResponse<BlogSummaryResponse> searchBlogs(String guestId, Long userId, BlogSearchCondition condition, @Nullable List<Long> followingIds) {
        // 추천순 쿼리 가져오기
        List<Query> recommendQueries = new ArrayList<>();
        if (condition.sortType() == BlogSortType.RECOMMEND) {
            recommendQueries = recommendService.getRecommendQueries(guestId, userId, PostType.BLOG);
        }

        BlogSearchResult result = blogDocQueryRepository.searchBlogs(condition, followingIds, recommendQueries);
        List<BlogDoc> docs = result.docs();
        if (docs.isEmpty()) {
            return new BlogSliceResponse<>(List.of(), false, null);
        }
        List<Long> blogIds = docs.stream()
                .map(BlogDoc::getId)
                .toList();

        // 좋아요/북마크 여부, 댓글수를 한 번에 조회 (N+1 해결)
        final Set<Long> likedIds =
                userId != null ? blogLikeService.findLikedBlogIds(userId, blogIds) : Set.of();
        final Set<Long> bookmarkedIds =
                userId != null ? blogBookmarkService.findBookmarkedBlogIds(userId, blogIds) : Set.of();
        final Map<Long, Long> commentCounts =
                commentsService.getCommentCounts(blogIds, CommentsTargetType.BLOG);

        List<BlogSummaryResponse> content = result.docs().stream()
                .map(doc -> new BlogSummaryResponse(doc, likedIds, bookmarkedIds, commentCounts))
                .toList();

        return new BlogSliceResponse<>(
                content,
                result.hasNext(),
                result.nextCursor()
        );
    }
}