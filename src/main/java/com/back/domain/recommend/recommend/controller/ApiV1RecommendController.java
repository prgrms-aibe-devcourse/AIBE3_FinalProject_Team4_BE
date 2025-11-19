package com.back.domain.recommend.recommend.controller;

import com.back.domain.recommend.recommend.PageResponse;
import com.back.domain.recommend.recommend.PostService;
import com.back.domain.recommend.recommend.service.RecommendService;
import com.back.domain.recommend.recommend.type.PostType;
import com.back.domain.shorlog.shorlogdoc.document.ShorlogDoc;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
public class ApiV1RecommendController {
    private final PostService postService;
    private final RecommendService recommendService;

    @GetMapping("/native")
    public List<ShorlogDoc> mainFeedWithNativeQuery(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        postService.createPost(null);
        return recommendService.getFeedWithNativeQuery(page, size);
    }

    @GetMapping
    public PageResponse<ShorlogDoc> mainFeedMap(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(recommendService.getPostsOrderByRecommendation(1L, page, size, PostType.SHORLOG));
    }
}