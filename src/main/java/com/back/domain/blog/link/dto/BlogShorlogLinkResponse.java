package com.back.domain.blog.link.dto;

public record BlogShorlogLinkResponse(
        Long blogId,
        Long shorlogId,
        boolean haveLink,
        int linkedCount
) {
}