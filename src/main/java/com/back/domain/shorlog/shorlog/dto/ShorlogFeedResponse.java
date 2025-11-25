package com.back.domain.shorlog.shorlog.dto;

import com.back.domain.shorlog.shorlog.entity.Shorlog;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
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

    public static ShorlogFeedResponse from(Shorlog shorlog, List<String> hashtags, Integer likeCount, Integer commentCount) {
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
                commentCount,
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