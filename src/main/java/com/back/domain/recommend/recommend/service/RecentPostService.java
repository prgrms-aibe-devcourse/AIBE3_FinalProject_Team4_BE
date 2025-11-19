package com.back.domain.recommend.recommend.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import com.back.domain.blog.blogdoc.document.BlogDoc;
import com.back.domain.recommend.recommend.type.PostType;
import com.back.domain.shorlog.shorlogdoc.document.ShorlogDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecentPostService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ElasticsearchClient esClient;

    private static final int MAX_SHORLOGS = 10;
    private static final int MAX_BLOGS = 5;

    public void addRecentPost(Long userId, Long postId, PostType type) {
        String key = buildRecentPostKey(userId, type);

        final int maxRecent = (type == PostType.SHORLOG) ? MAX_SHORLOGS : MAX_BLOGS;

        stringRedisTemplate.opsForList().remove(key, 1, postId);
        stringRedisTemplate.opsForList().leftPush(key, postId.toString());

        stringRedisTemplate.opsForList().trim(key, 0, maxRecent - 1);
        stringRedisTemplate.expire(key, Duration.ofDays(7));
    }

    public List<Long> getRecentPosts(Long userId, PostType type) {
        String key = buildRecentPostKey(userId, type);

        final int limit = (type == PostType.SHORLOG) ? MAX_SHORLOGS : MAX_BLOGS;

        return stringRedisTemplate.opsForList().range(key, 0, limit - 1)
                .stream()
                .map(Long::parseLong)
                .toList();
    }

    public List<String> getRecentContents(List<Long> postIds, PostType type) {
        if (postIds == null || postIds.isEmpty()) return List.of();

        final int limit = (type == PostType.SHORLOG) ? MAX_SHORLOGS : MAX_BLOGS;

        List<Long> limited = postIds.stream()
                .limit(limit)
                .toList();

        Function<Long, String> contentLoader = switch (type) {
            case SHORLOG -> this::loadShorlogContent;
            case BLOG -> this::loadBlogContent;
        };

        return limited.stream()
                .map(contentLoader)
                .filter(Objects::nonNull)
                .toList();
    }

    private String buildRecentPostKey(Long userId, PostType type) {
        String typeSuffix = switch (type) {
            case SHORLOG -> "shorlogs";
            case BLOG -> "blogs";
        };

        return "user:" + userId + ":recent_" + typeSuffix;
    }

    private String loadShorlogContent(Long shorlogId) {
        try {
            GetResponse<ShorlogDoc> response = esClient.get(
                    g -> g.index("app1_shorlogs").id(shorlogId.toString()),
                    ShorlogDoc.class
            );

            if (response.found() && response.source() != null) {
                return response.source().getContent();
            }
            return null;

        } catch (IOException e) {
            log.error("ES 조회 실패: shorlogId={}", shorlogId, e);
            return null;
        }
    }

    private String loadBlogContent(Long blogId) {
        try {
            GetResponse<BlogDoc> response = esClient.get(
                    g -> g.index("app1_blogs").id(blogId.toString()),
                    BlogDoc.class
            );

            if (response.found() && response.source() != null) {
                return response.source().getContent();
            }
            return null;

        } catch (IOException e) {
            log.error("ES 조회 실패: blogId={}", blogId, e);
            return null;
        }
    }
}
