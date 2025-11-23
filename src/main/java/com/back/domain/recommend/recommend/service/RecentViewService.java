package com.back.domain.recommend.recommend.service;

import com.back.domain.recommend.recommend.PostType;
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

    public void addRecentViewPost(String guestId, PostType postType, Long postId) {
        String key = buildKey(true, guestId, postType);

        stringRedisTemplate.opsForList().remove(key, 0, postId.toString());
        stringRedisTemplate.opsForList().leftPush(key, postId.toString());

        int limit = postType.getSearchLimit();
        stringRedisTemplate.opsForList().trim(key, 0, limit - 1);
        stringRedisTemplate.expire(key, Duration.ofDays(14));
    }

    public void mergeGuestHistoryToUser(String guestId, Long userId, PostType postType) {
        String GUEST_KEY = buildKey(true, guestId, postType);
        String USER_KEY = buildKey(false, userId.toString(), postType);

        List<String> guestHistory = stringRedisTemplate.opsForList().range(GUEST_KEY, 0, -1);
        if (guestHistory == null) guestHistory = List.of();

        List<String> userHistory = stringRedisTemplate.opsForList().range(USER_KEY, 0, -1);
        if (userHistory == null) userHistory = List.of();

        Set<String> mergedSet = new LinkedHashSet<>();
        mergedSet.addAll(guestHistory);
        mergedSet.addAll(userHistory);

        int limit = postType.getSearchLimit();
        List<String> finalHistory = mergedSet.stream()
                .limit(limit)
                .toList();

        stringRedisTemplate.delete(USER_KEY);
        stringRedisTemplate.opsForList().rightPushAll(USER_KEY, finalHistory);

        stringRedisTemplate.delete(GUEST_KEY);
    }

    public List<Long> getRecentViewPosts(String guestId, Long userId, PostType postType) {
        return getRecentViewPosts(guestId, userId, postType, -1);
    }

    public List<Long> getRecentViewPosts(String guestId, Long userId, PostType postType, int limit) {
        String identifier = getIdentifier(guestId, userId);
        String key = buildKey(isGuest(userId), identifier, postType);

        int finalLimit = getValidLimit(limit, postType.getSearchLimit());

        return Objects.requireNonNull(stringRedisTemplate.opsForList().range(key, 0, finalLimit - 1))
                .stream()
                .map(Long::parseLong)
                .toList();
    }


    private boolean isGuest(Long userId) {
        return userId == null || userId == 0;
    }

    private String getIdentifier(String guestId, Long userId) {
        if (!isGuest(userId)) {
            return userId.toString();
        }
        return guestId;
    }

    private String buildKey(boolean isGuest, String identifier, PostType postType) {
        String userStatus = (isGuest) ? "guest" : "user";
        String postTypeName = (postType == PostType.SHORLOG) ? "shorlogs" : "blogs";

        return "recent_view_" + postTypeName + ":" + userStatus + ":" + identifier;
    }

    private int getValidLimit(int limit, int defaultLimit) {
        if (limit < 1 || limit > defaultLimit) {
            return defaultLimit;
        }
        return limit;
    }
}
