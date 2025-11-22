package com.back.domain.recommend.recommend;

import com.back.domain.blog.blogdoc.document.BlogDoc;
import com.back.domain.shorlog.shorlogdoc.document.ShorlogDoc;
import lombok.Getter;

import java.util.List;

@Getter
public enum PostType {
    SHORLOG(
            ShorlogDoc.class,
            "app1_shorlogs",
            List.of("content", "hashtags"),
            10
    ),

    BLOG(
            BlogDoc.class,
            "app1_blogs",
            List.of("content", "title", "hashtagName"),
            5
    );

    private final Class<?> documentClass;
    private final String indexName;
    private final List<String> searchFields;
    private final int searchLimit;

    PostType(Class<?> documentClass, String indexName, List<String> searchFields, int searchLimit) {
        this.documentClass = documentClass;
        this.indexName = indexName;
        this.searchFields = searchFields;
        this.searchLimit = searchLimit;
    }
}
