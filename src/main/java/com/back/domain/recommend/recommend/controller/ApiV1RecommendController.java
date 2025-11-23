package com.back.domain.recommend.recommend.controller;

import com.back.domain.recommend.recommend.PostService;
import com.back.domain.recommend.recommend.PostType;
import com.back.domain.recommend.recommend.service.RecentViewService;
import com.back.domain.recommend.recommend.service.RecommendService;
import com.back.domain.user.activity.service.UserActivityService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.back.domain.recommend.recommend.constants.GuestConstants.GUEST_COOKIE_MAX_AGE;
import static com.back.domain.recommend.recommend.constants.GuestConstants.GUEST_COOKIE_NAME;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ApiV1RecommendController {
    private final Rq rq;
    private final PostService postService;
    private final RecommendService recommendService;
    private final RecentViewService recentViewService;
    private final UserActivityService userActivityService;

//    @GetMapping("/test/user-activity/{userId}")
//    public Map<String, List<?>> getUserAllActivitiesRaw(
//            @PathVariable Long userId,
//            @RequestParam(defaultValue = "true") boolean isShorlog) {
//
//        // 1. 좋아요 기반
//        List<?> likedPosts = userActivityService.getUserLikedPosts(userId, isShorlog);
//
//        // 2. 북마크 기반
//        List<?> bookmarkedPosts = userActivityService.getUserBookmarkedPosts(userId, isShorlog);
//
//        // 3. 댓글 기반 (UserCommentActivityDto 반환)
//        List<?> commentedPosts = userActivityService.getUserCommentedPosts(userId, isShorlog);
//
//        // 4. 작성글 기반
//        List<?> writtenPosts = userActivityService.getUserWrittenPosts(userId, isShorlog);
//
//        // 모든 결과를 하나의 Map으로 묶어 반환합니다.
//        Map<String, List<?>> response = new HashMap<>();
//        response.put("likedPosts", likedPosts);
//        response.put("bookmarkedPosts", bookmarkedPosts);
//        response.put("commentedPosts", commentedPosts);
//        response.put("writtenPosts", writtenPosts);
//
//        return response;
//    }
//
//    @GetMapping("/test/es-save")
//    public void save() {
//        postService.createPost(null);
//    }
//
//    @GetMapping("/test/es-delete")
//    public void delete() {
//        postService.deleteAll();
//    }

    @GetMapping("/posts/feed")
    public Page<?> mainFeed(@CookieValue(value = GUEST_COOKIE_NAME, required = false) String guestId,
                            @AuthenticationPrincipal SecurityUser securityUser,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "30") int size) {
        Long userId = (securityUser == null) ? 0 : securityUser.getId();
        return recommendService.getPostsOrderByRecommend(guestId, userId, page, size, PostType.SHORLOG);
    }

    @GetMapping("/shorlog/{postId}/view")
    public RsData<Void> viewShorlog(@CookieValue(value = GUEST_COOKIE_NAME, required = false) String guestId,
                                    @AuthenticationPrincipal SecurityUser securityUser,
                                    @PathVariable Long postId) {
        if (postId < 1) {
            throw new IllegalArgumentException("ID가 유효하지 않습니다.");
        }

        boolean hasGuestId = (guestId != null);

        if (!hasGuestId) {
            guestId = UUID.randomUUID().toString();
            rq.setCookie(GUEST_COOKIE_NAME, guestId, GUEST_COOKIE_MAX_AGE);
        }

        recentViewService.addRecentViewPost(guestId, PostType.SHORLOG, postId);

        if (securityUser != null) {
            Long userId = securityUser.getId();
            recentViewService.mergeGuestHistoryToUser(guestId, userId, PostType.SHORLOG);
        }

        return RsData.successOf(null);
    }

    @GetMapping("/blogs/{postId}/view")
    public RsData<Void> viewBlog() {
        return RsData.successOf(null);
    }
}