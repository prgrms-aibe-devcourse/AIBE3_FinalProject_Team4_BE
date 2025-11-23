package com.back.domain.recommend.recommend.controller;

import com.back.domain.recommend.recommend.PageResponse;
import com.back.domain.recommend.recommend.PostService;
import com.back.domain.recommend.recommend.PostType;
import com.back.domain.recommend.recommend.service.RecentViewService;
import com.back.domain.recommend.recommend.service.RecommendService;
import com.back.domain.shorlog.shorlogdoc.document.ShorlogDoc;
import com.back.global.config.security.SecurityUser;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ApiV1RecommendController {
    public static final String GUEST_COOKIE_NAME = "guestId";
    public static final int GUEST_COOKIE_MAX_AGE = 60 * 60 * 24 * 30; // 30일

    private final Rq rq;
    private final PostService postService;
    private final RecommendService recommendService;
    private final RecentViewService recentViewService;

    @GetMapping("/test/es-save")
    public void save() {
        postService.createPost(null);
    }

    @GetMapping("/test/es-delete")
    public void delete() {
        postService.deleteAll();
    }

    @GetMapping("/posts/feed")
    public PageResponse<ShorlogDoc> mainFeed(@CookieValue(value = GUEST_COOKIE_NAME, required = false) String guestId,
                                             @AuthenticationPrincipal SecurityUser securityUser,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        Long userId = (securityUser == null) ? 0 : securityUser.getId();
        return PageResponse.from(recommendService.getPostsOrderByRecommend(guestId, userId, page, size, PostType.SHORLOG, ShorlogDoc.class));
    }

    @GetMapping("/shorlog/{postId}/view")
    public RsData<Void> viewShorlog(@CookieValue(value = GUEST_COOKIE_NAME, required = false) String guestId,
                                    @AuthenticationPrincipal SecurityUser securityUser,
                                    @PathVariable Long postId) {
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