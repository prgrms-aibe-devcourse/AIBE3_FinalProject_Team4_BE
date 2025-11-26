package com.back.domain.shorlog.shorloglike.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShorlogLikeResponse {

    private Boolean isLiked;
    private Long likeCount;
}

