package com.back.domain.recommend.recommend;

import lombok.Getter;

import java.util.List;

@Getter
public enum PostType {

    SHORLOG(
            "app1_shorlogs",
            List.of("content", "hashtags")
    ),

    BLOG(
            "app1_blogs",
            List.of("content", "title", "hashtagName")
    );

    private final String indexName;
    private final List<String> searchFields;

    PostType(String indexName, List<String> searchFields) {
        this.indexName = indexName;
        this.searchFields = searchFields;
    }
}
