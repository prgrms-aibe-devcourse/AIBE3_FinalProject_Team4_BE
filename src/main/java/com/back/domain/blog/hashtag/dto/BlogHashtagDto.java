package com.back.domain.blog.hashtag.dto;

import com.back.domain.hashtag.hashtag.entity.Hashtag;

public record BlogHashtagDto(
        Long id,
        String name
) {
    public BlogHashtagDto(Hashtag blogHashtag) {
        this(
                blogHashtag.getId(),
                blogHashtag.getName()
        );
    }
}
