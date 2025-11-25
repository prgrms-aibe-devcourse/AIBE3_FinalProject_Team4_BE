package com.back.domain.notification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    BLOG_BOOKMARK("님이 회원님의 블로그를 북마크했습니다."),
    BLOG_LIKE("님이 회원님의 블로그를 좋아했습니다."),
    BLOG_COMMENT("님이 게시글에 댓글을 달았습니다."),

    SHORLOG_COMMENT("님이 쇼로그에 댓글을 달았습니다."),
    SHORLOG_LIKE("내 숏로그를 좋아했습니다."),
    SHORLOG_BOOKMARK("내 숏로그를 저장했습니다."),

    COMMENT_REPLY("님이 회원님의 댓글에 답글을 남겼습니다."),

    FOLLOW("님이 회원님을 팔로우했습니다.");

    private final String messageTemplate;

    // 자동 메시지 생성
    public String createMessage(String senderNickname) {
        return senderNickname + messageTemplate;
    }
}
