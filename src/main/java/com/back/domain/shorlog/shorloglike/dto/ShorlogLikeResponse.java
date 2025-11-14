package com.back.domain.shorlog.shorloglike.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShorlogLikeResponse {

    private Long likeCount;

    private Boolean isLiked;
}

