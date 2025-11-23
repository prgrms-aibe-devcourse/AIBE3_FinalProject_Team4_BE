package com.back.domain.recommend.recommend;

import com.back.domain.recommend.recommend.dto.RecommendShorlogResultDto;
import lombok.Getter;

import java.util.List;

@Getter
public enum PostType {
    SHORLOG(
            "app1_shorlogs",
            List.of("content", "hashtags"),
            10,
            List.of("id", "content", "thumbnailUrl", "profileImgUrl", "nickname", "hashtags", "likeCount", "commentCount"),
            RecommendShorlogResultDto.class
    ),

    BLOG(
            "app1_blogs",
            List.of("content", "title", "hashtagName"),
            5,
            List.of("id", "title"),
            RecommendShorlogResultDto.class
    );

    private final String indexName;
    private final List<String> searchFields;
    private final int searchLimit;
    private final List<String> resultFields;
    private final Class<?> resultType;

    PostType(String indexName, List<String> searchFields, int searchLimit, List<String> resultFields, Class<?> resultType) {
        this.indexName = indexName;
        this.searchFields = searchFields;
        this.searchLimit = searchLimit;
        this.resultFields = resultFields;
        this.resultType = resultType;
    }
}
