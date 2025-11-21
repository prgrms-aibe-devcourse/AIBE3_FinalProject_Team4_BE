package com.back.domain.recommend.recommend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecentViewService {

    private final StringRedisTemplate stringRedisTemplate;
    private final PostDocService postDocService;

    private static final int MAX_SHORLOGS = 10;
    private static final int MAX_BLOGS = 5;

    public void addRecentPost(boolean isGuest, Long userId, String guestId, boolean isShorlog, Long postId) {
        String identifier = (isGuest) ? guestId : userId.toString();
        String key = buildRecentPostKey(isGuest, identifier, isShorlog);

        stringRedisTemplate.opsForList().remove(key, 0, postId.toString());
        stringRedisTemplate.opsForList().leftPush(key, postId.toString());

        int limit = getLimit(isShorlog);
        stringRedisTemplate.opsForList().trim(key, 0, limit - 1);
        stringRedisTemplate.expire(key, Duration.ofDays(7));
    }

    public void mergeRecentPosts(String guestId, Long userId, boolean isShorlog) {
        String GUEST_KEY = buildRecentPostKey(true, guestId, isShorlog);
        String USER_KEY = buildRecentPostKey(false, userId.toString(), isShorlog);

        List<String> guestHistory = stringRedisTemplate.opsForList().range(GUEST_KEY, 0, -1);
        if (guestHistory == null) guestHistory = List.of();

        List<String> userHistory = stringRedisTemplate.opsForList().range(USER_KEY, 0, -1);
        if (userHistory == null) userHistory = List.of();

        Set<String> mergedSet = new LinkedHashSet<>();
        mergedSet.addAll(userHistory);
        mergedSet.addAll(guestHistory);

        int limit = getLimit(isShorlog);
        List<String> finalHistory = mergedSet.stream()
                .limit(limit)
                .toList();

        if (!finalHistory.isEmpty()) {
            stringRedisTemplate.delete(USER_KEY);
            stringRedisTemplate.opsForList().leftPushAll(USER_KEY, finalHistory);
        }

        stringRedisTemplate.delete(GUEST_KEY);
    }

    public List<Long> getRecentPosts(String guestId, Long userId, boolean isShorlog) {
        String identifier = getIdentifier(guestId, userId);
        String key = buildRecentPostKey(isGuest(userId), identifier, isShorlog);

        int limit = getLimit(isShorlog);
        return Objects.requireNonNull(stringRedisTemplate.opsForList().range(key, 0, limit - 1))
                .stream()
                .map(Long::parseLong)
                .toList();
    }

    public List<String> getRecentContents(boolean isShorlog, List<Long> postIds) {
        return getRecentContents(isShorlog, postIds, 0);
    }

    public List<String> getRecentContents(boolean isShorlog, List<Long> postIds, int limit) {
        if (postIds == null || postIds.isEmpty()) return List.of();

        if ((limit < 1) || (limit > MAX_SHORLOGS) || (!isShorlog && (limit > MAX_BLOGS))) {
            limit = getLimit(isShorlog);
        }

        List<Long> limited = postIds.stream()
                .limit(limit)
                .toList();

        return limited.stream()
                .map(postId -> postDocService.getContent(isShorlog, postId))
                .filter(Objects::nonNull)
                .toList();
    }

    private String buildRecentPostKey(boolean isGuest, String identifier, boolean isShorlog) {
        String userStatus = (isGuest) ? "guest" : "user";
        String postType = (isShorlog) ? "shorlogs" : "blogs";

        return userStatus + ":" + identifier + ":recent_view_" + postType;
    }

    private String getIdentifier(String guestId, Long userId) {
        if (!isGuest(userId)) {
            return userId.toString();
        }
        return guestId;
    }

    private boolean isGuest(Long userId) {
        return userId == null || userId == 0;
    }

    private int getLimit(boolean isShorlog) {
        return (isShorlog) ? MAX_SHORLOGS : MAX_BLOGS;
    }
}
