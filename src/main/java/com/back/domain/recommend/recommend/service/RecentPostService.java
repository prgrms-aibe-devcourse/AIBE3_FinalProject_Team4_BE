package com.back.domain.recommend.recommend.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import com.back.domain.recommend.recommend.type.PostType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.back.domain.recommend.recommend.constants.PostConstants.BLOG_INDEX_NAME;
import static com.back.domain.recommend.recommend.constants.PostConstants.SHORLOG_INDEX_NAME;

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

        stringRedisTemplate.opsForList().remove(key, 1, postId.toString());
        stringRedisTemplate.opsForList().leftPush(key, postId.toString());

        stringRedisTemplate.opsForList().trim(key, 0, maxRecent - 1);
        stringRedisTemplate.expire(key, Duration.ofDays(7));
    }

    public List<Long> getRecentPosts(Long userId, PostType type) {
        String key = buildRecentPostKey(userId, type);

        final int limit = (type == PostType.SHORLOG) ? MAX_SHORLOGS : MAX_BLOGS;

        return Objects.requireNonNull(stringRedisTemplate.opsForList().range(key, 0, limit - 1))
                .stream()
                .map(Long::parseLong)
                .toList();
    }

    public List<String> getRecentContents(List<Long> postIds, PostType type) {
        int limit = (type == PostType.SHORLOG) ? MAX_SHORLOGS : MAX_BLOGS;

        return getRecentContents(postIds, type, limit);
    }

    public List<String> getRecentContents(List<Long> postIds, PostType type, int limit) {
        if (postIds == null || postIds.isEmpty()) return List.of();

        if (limit < 1 || limit > MAX_SHORLOGS) {
            limit = (type == PostType.SHORLOG) ? MAX_SHORLOGS : MAX_BLOGS;
        }

        List<Long> limited = postIds.stream()
                .limit(limit)
                .toList();

        return limited.stream()
                .map(postId -> loadContent(postId, type))
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

    private String loadContent(Long postId, PostType type) {
        final String indexName = (type == PostType.SHORLOG) ? SHORLOG_INDEX_NAME : BLOG_INDEX_NAME;
        try {
            GetResponse<Map> response = esClient.get(
                    g -> g.index(indexName).id(postId.toString()),
                    Map.class
            );

            if (response.found() && response.source() != null) {
                return (String) response.source().get("content");
            }
            return null;

        } catch (IOException e) {
            log.error("ES 조회 실패: id({})={}", type, postId, e);
            return null;
        }
    }
}
