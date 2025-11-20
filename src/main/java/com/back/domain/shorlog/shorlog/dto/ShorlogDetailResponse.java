package com.back.domain.shorlog.shorlog.dto;

import com.back.domain.shorlog.shorlog.entity.Shorlog;
import lombok.AllArgsConstructor;
 import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ShorlogDetailResponse {

    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String profileImgUrl;
    private String content;
    private List<String> thumbnailUrls;
    private Integer viewCount;
    private Integer likeCount;
    private Integer bookmarkCount;
    private Integer commentCount;
    private List<String> hashtags;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Long linkedBlogId;

    public static ShorlogDetailResponse from(Shorlog shorlog, List<String> hashtags, Integer viewCount,
                                             Integer likeCount, Integer bookmarkCount, Long linkedBlogId) {
        return new ShorlogDetailResponse(
                shorlog.getId(),
                shorlog.getUser().getId(),
                shorlog.getUser().getUsername(),
                shorlog.getUser().getNickname(),
                shorlog.getUser().getProfileImgUrl(),
                shorlog.getContent(),
                shorlog.getThumbnailUrlList() != null ? List.copyOf(shorlog.getThumbnailUrlList()) : List.of(),
                viewCount,
                likeCount,
                bookmarkCount,
                0, // TODO: 댓글 기능 구현 후 (4번 이해민)
                hashtags != null ? List.copyOf(hashtags) : List.of(),
                shorlog.getCreatedAt(),
                shorlog.getModifiedAt(),
                linkedBlogId  // null 또는 blogId
        );
    }
}
