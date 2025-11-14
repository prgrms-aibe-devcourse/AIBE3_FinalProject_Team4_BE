package com.back.domain.notification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    COMMENT("님이 회원님의 게시글에 댓글을 남겼습니다."),
    REPLY("님이 회원님의 댓글에 답글을 남겼습니다."),
    LIKE_POST("님이 회원님의 게시글을 좋아했습니다."),
    LIKE_COMMENT("님이 회원님의 댓글을 좋아했습니다."),
    FOLLOW("님이 회원님을 팔로우하기 시작했습니다."),
    BOOKMARK("님이 회원님의 게시글을 북마크했습니다."),
    BLOG_BOOKMARK("님이 회원님의 블로그를 북마크했습니다."),
    BLOG_LIKE("님이 회원님의 블로그를 좋아했습니다.");

    private final String messageTemplate;

    // 자동 메시지 생성
    public String createMessage(String senderNickname) {
        return senderNickname + messageTemplate;
    }
}
