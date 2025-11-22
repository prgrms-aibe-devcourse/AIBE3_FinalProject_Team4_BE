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
    private final PostDocService postDocService;


    public void addRecentPost(boolean isGuest, Long userId, String guestId, PostType postType, Long postId) {
        String identifier = (isGuest) ? guestId : userId.toString();
        String key = buildRecentPostKey(isGuest, identifier, postType);

        stringRedisTemplate.opsForList().remove(key, 0, postId.toString());
        stringRedisTemplate.opsForList().leftPush(key, postId.toString());

        int limit = postType.getSearchLimit();
        stringRedisTemplate.opsForList().trim(key, 0, limit - 1);
        stringRedisTemplate.expire(key, Duration.ofDays(7));
    }

    public void mergeRecentPosts(String guestId, Long userId, PostType postType) {
        String GUEST_KEY = buildRecentPostKey(true, guestId, postType);
        String USER_KEY = buildRecentPostKey(false, userId.toString(), postType);

        List<String> guestHistory = stringRedisTemplate.opsForList().range(GUEST_KEY, 0, -1);
        if (guestHistory == null) guestHistory = List.of();

        List<String> userHistory = stringRedisTemplate.opsForList().range(USER_KEY, 0, -1);
        if (userHistory == null) userHistory = List.of();

        Set<String> mergedSet = new LinkedHashSet<>();
        mergedSet.addAll(userHistory);
        mergedSet.addAll(guestHistory);

        int limit = postType.getSearchLimit();
        List<String> finalHistory = mergedSet.stream()
                .limit(limit)
                .toList();

        if (!finalHistory.isEmpty()) {
            stringRedisTemplate.delete(USER_KEY);
            stringRedisTemplate.opsForList().leftPushAll(USER_KEY, finalHistory);
        }

        stringRedisTemplate.delete(GUEST_KEY);
    }

    public List<Long> getRecentPosts(String guestId, Long userId, PostType postType) {
        String identifier = getIdentifier(guestId, userId);
        String key = buildRecentPostKey(isGuest(userId), identifier, postType);

        int limit = postType.getSearchLimit();
        return Objects.requireNonNull(stringRedisTemplate.opsForList().range(key, 0, limit - 1))
                .stream()
                .map(Long::parseLong)
                .toList();
    }

    public List<String> getRecentContents(PostType postType, List<Long> postIds) {
        return getRecentContents(postType, postIds, 0);
    }

    public List<String> getRecentContents(PostType postType, List<Long> postIds, int limit) {
        if (postIds == null || postIds.isEmpty()) return List.of();

        if ((limit < 1) || (limit > postType.getSearchLimit())) {
            limit = postType.getSearchLimit();
        }

        List<Long> limited = postIds.stream()
                .limit(limit)
                .toList();

        return limited.stream()
                .map(postId -> postDocService.getContent(postType, postId))
                .filter(Objects::nonNull)
                .toList();
    }

    private String buildRecentPostKey(boolean isGuest, String identifier, PostType postType) {
        String userStatus = (isGuest) ? "guest" : "user";
        String postTypeName = (postType == PostType.SHORLOG) ? "shorlogs" : "blogs";

        return userStatus + ":" + identifier + ":recent_view_" + postTypeName;
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
}
