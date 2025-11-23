package com.back.domain.recommend.recommend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RecommendShorlogResultDto {
    private Long id;
    private String content;
    private String thumbnailUrl;
    private String profileImgUrl;
    private String nickname;
    private List<String> hashtags;
    private Integer likeCount;
    private Integer commentCount;
}
