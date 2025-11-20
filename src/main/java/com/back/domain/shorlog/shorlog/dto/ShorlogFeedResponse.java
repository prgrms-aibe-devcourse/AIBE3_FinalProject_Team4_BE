package com.back.domain.shorlog.shorlog.dto;

import com.back.domain.shorlog.shorlog.entity.Shorlog;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ShorlogFeedResponse {

    private Long id;
    private String thumbnailUrl;
    private String profileImgUrl;
    private String nickname;
    private List<String> hashtags;
    private Integer likeCount;
    private Integer commentCount;
    private String firstLine;

    public static ShorlogFeedResponse from(Shorlog shorlog, List<String> hashtags, Integer likeCount) {
        List<String> thumbnailUrls = shorlog.getThumbnailUrlList();
        String firstThumbnail = (thumbnailUrls != null && !thumbnailUrls.isEmpty())
                ? thumbnailUrls.getFirst()
                : null;

        return new ShorlogFeedResponse(
                shorlog.getId(),
                firstThumbnail,
                shorlog.getUser().getProfileImgUrl(),
                shorlog.getUser().getNickname(),
                hashtags != null ? List.copyOf(hashtags) : List.of(),
                likeCount,
                0, // TODO: 댓글 기능 구현 후 (4번 이해민)
                extractFirstLine(shorlog.getContent())
        );
    }

    // 콘텐츠에서 첫 줄 추출 - 줄바꿈(\n) 기준으로 첫 줄만 반환
    public static String extractFirstLine(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        String[] lines = content.split("\n");
        return lines[0];
    }
}