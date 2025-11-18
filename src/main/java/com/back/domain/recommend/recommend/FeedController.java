package com.back.domain.recommend.recommend;

import com.back.domain.shorlog.shorlogdoc.document.ShorlogDoc;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {
    private final PostService postService;
    private final FeedService feedService;

    @GetMapping("/main")
    public List<ShorlogDoc> mainFeed(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        postService.createPost(null);
        return feedService.getMainFeed(page, size);
    }
}