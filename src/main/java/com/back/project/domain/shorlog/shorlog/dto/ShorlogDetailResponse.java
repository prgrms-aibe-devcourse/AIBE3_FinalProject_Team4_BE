package com.back.project.domain.shorlog.shorlog.dto;

import com.back.project.domain.shorlog.shorlog.entity.Shorlog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class ShorlogDetailResponse {

    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String profileImgUrl;
    private String content;
    private String thumbnailUrl;
    private String thumbnailType;
    private Integer viewCount;
    private Integer likeCount;
    private Integer bookmarkCount;
    private Integer commentCount;
    private List<String> hashtags;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Long connectedBlogId;

    public static ShorlogDetailResponse from(Shorlog shorlog, List<String> hashtags) {
        return ShorlogDetailResponse.builder()
                .id(shorlog.getId())
                .userId(shorlog.getUser().getId())
                .username(shorlog.getUser().getUsername())
                .nickname(shorlog.getUser().getNickname())
                .profileImgUrl(shorlog.getUser().getProfileimgurl())
                .content(shorlog.getContent())
                .thumbnailUrl(shorlog.getThumbnailUrl())
                .thumbnailType(shorlog.getThumbnailType())
                .viewCount(shorlog.getViewCount())
                .likeCount(0) // TODO: 좋아요 기능 구현 후
                .bookmarkCount(0) // TODO: 북마크 기능 구현 후
                .commentCount(0) // TODO: 댓글 기능 구현 후 (4번 이해민)
                .hashtags(hashtags)
                .createdAt(shorlog.getCreateDate())
                .modifiedAt(shorlog.getModifyDate())
                .connectedBlogId(null) // TODO: 블로그 연결 기능 구현 후
                .build();
    }
}
