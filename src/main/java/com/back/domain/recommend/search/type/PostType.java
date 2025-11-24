package com.back.domain.recommend.search.type;

import lombok.Getter;

import java.util.List;

@Getter
public enum PostType {
    SHORLOG(
            "app1_shorlogs",
            List.of("content", "hashtags"),
            10,
            List.of("id", "content", "thumbnailUrl", "profileImgUrl", "nickname", "hashtags", "likeCount", "commentCount")
    ),

    BLOG(
            "app1_blogs",
            List.of("content", "title", "hashtagName"),
            5,
            List.of()
    );

    private final String indexName;
    private final List<String> searchFields;
    private final int searchLimit;
    private final List<String> resultFields;

    PostType(String indexName, List<String> searchFields, int searchLimit, List<String> resultFields) {
        this.indexName = indexName;
        this.searchFields = searchFields;
        this.searchLimit = searchLimit;
        this.resultFields = resultFields;
    }
}
