package com.back.domain.recommend.recommend.controller;

import com.back.domain.recommend.recommend.PageResponse;
import com.back.domain.recommend.recommend.PostService;
import com.back.domain.recommend.recommend.service.RecentPostService;
import com.back.domain.recommend.recommend.service.RecommendService;
import com.back.domain.recommend.recommend.type.PostType;
import com.back.domain.shorlog.shorlogdoc.document.ShorlogDoc;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ApiV1RecommendController {
    private final PostService postService;
    private final RecommendService recommendService;
    private final RecentPostService recentPostService;

    @GetMapping("/test/save-es")
    public void save() {
        postService.createPost(null);
    }

    @GetMapping("/test/delete-es")
    public void delete() {
        postService.deleteAll();
    }

    @GetMapping("/test/feed-native")
    public List<ShorlogDoc> mainFeedWithNativeQuery(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return recommendService.getFeedWithNativeQuery(page, size);
    }

    @GetMapping("/test/feed-native-knn3")
    public void searchKnn3(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size) {
        recommendService.searchKnn3(1L, page, size, PostType.SHORLOG);
    }

    @GetMapping("/posts/feed")
    public PageResponse<ShorlogDoc> mainFeedMap(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return PageResponse.from(recommendService.getPostsOrderByRecommendation(1L, page, size, PostType.SHORLOG));
    }

    @GetMapping("/posts/{postId}/view")
    public RsData<Void> viewPost(@PathVariable long postId, @RequestParam PostType type) {
        recentPostService.addRecentPost(1L, postId, type);
        return RsData.successOf(null);
    }
}