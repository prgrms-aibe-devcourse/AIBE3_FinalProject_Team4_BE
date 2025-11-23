package com.back.domain.recommend.recommend.controller;

import com.back.domain.recommend.recentview.service.RecentViewService;
import com.back.domain.recommend.search.type.PostType;
import com.back.domain.shorlog.shorlogdoc.repository.ShorlogDocRepository;
import com.back.global.config.security.SecurityUser;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.back.domain.recommend.recentview.constants.GuestConstants.GUEST_COOKIE_MAX_AGE;
import static com.back.domain.recommend.recentview.constants.GuestConstants.GUEST_COOKIE_NAME;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ApiV1RecommendController {
    private final Rq rq;
    private final RecentViewService recentViewService;
    private final ShorlogDocRepository shorlogDocRepository;

    @GetMapping("/test")
    public RsData<Void> test() {
        shorlogDocRepository.deleteAll();
        return RsData.successOf(null);
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