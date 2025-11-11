package com.back.domain.shorlog.shorlog.dto;

import com.back.domain.shorlog.shorlog.entity.Shorlog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class ShorlogFeedResponse {

    private Long id;
    private String thumbnailUrl; // 피드에서는 첫 번째 섬네일만 표시
    private String profileImgUrl;
    private String username;
    private List<String> hashtags;
    private Integer likeCount;
    private Integer commentCount;
    private String firstLine;

    public static ShorlogFeedResponse from(Shorlog shorlog, List<String> hashtags) {
        List<String> thumbnailUrls = shorlog.getThumbnailUrlList();
        String firstThumbnail = (thumbnailUrls != null && !thumbnailUrls.isEmpty())
                ? thumbnailUrls.get(0)
                : null;

        return ShorlogFeedResponse.builder()
                .id(shorlog.getId())
                .thumbnailUrl(firstThumbnail)
                .profileImgUrl(shorlog.getUser().getProfileimgurl())
                .username(shorlog.getUser().getUsername())
                .hashtags(hashtags)
                .likeCount(0) // TODO: 좋아요 기능 구현 후
                .commentCount(0) // TODO: 댓글 기능 구현 후 (4번 이해민)
                .firstLine(extractFirstLine(shorlog.getContent()))
                .build();
    }

    // 콘텐츠에서 첫 문장 추출 - 줄바꿈(\n) 기준으로 첫 줄만 반환
    private static String extractFirstLine(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        String[] lines = content.split("\n");
        return lines[0];
    }
}