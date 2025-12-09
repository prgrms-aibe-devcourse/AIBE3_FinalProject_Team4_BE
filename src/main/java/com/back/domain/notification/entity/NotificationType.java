package com.back.domain.notification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    // ---------------- 블로그 ----------------
    BLOG_BOOKMARK("님이 회원님의 블로그를 북마크했습니다."),
    BLOG_LIKE("님이 회원님의 블로그를 좋아합니다."),
    BLOG_COMMENT("님이 회원님의 블로그에 댓글을 달았습니다."),

    // ---------------- 숏로그 ----------------
    SHORLOG_COMMENT("님이 회원님의 숏로그에 댓글을 달았습니다."),
    SHORLOG_LIKE("님이 회원님의 숏로그를 좋아합니다."),
    SHORLOG_BOOKMARK("님이 회원님의 숏로그를 저장했습니다."),

    // ---------------- 댓글 기반 ----------------
    COMMENT_REPLY("님이 회원님의 댓글에 답글을 남겼습니다."),
    MENTION("님이 회원님을 멘션했습니다."),

    // ---------------- 팔로우 ----------------
    FOLLOW("님이 회원님을 팔로우했습니다.");

    private final String messageTemplate;

    public String createMessage(String senderNickname) {
        return senderNickname + messageTemplate;
    }
}
