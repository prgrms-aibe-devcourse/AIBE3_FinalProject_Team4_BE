package com.back.domain.notification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    BLOG_BOOKMARK("님이 회원님의 블로그를 북마크했습니다."),
    BLOG_LIKE("님이 회원님의 블로그를 좋아합니다."),
    BLOG_COMMENT("님이 블로그에 댓글을 달았습니다."),

    SHORLOG_COMMENT("님이 숏로그에 댓글을 달았습니다."),
    SHORLOG_LIKE("님이 회원님의 숏로그를 좋아합니다."),
    SHORLOG_BOOKMARK("님이 숏로그를 저장했습니다."),

    COMMENT_REPLY("님이 회원님의 댓글에 답글을 남겼습니다."),

    FOLLOW("님이 회원님을 팔로우했습니다.");

    private final String messageTemplate;

    // 🔥 1) 자동 메시지 생성
    public String createMessage(String senderNickname) {
        return senderNickname + messageTemplate;
    }

    // 🔥 2) 각 알림 타입별 redirect URL 생성 메서드
    public String buildRedirectUrl(Long targetId) {
        return switch (this) {

            // -------------- 블로그 --------------
            case BLOG_LIKE,
                 BLOG_BOOKMARK ->
                    "/blogs/" + targetId;

            case BLOG_COMMENT,
                 COMMENT_REPLY ->
                    "/blogs/" + targetId + "?focus=comment";

            // -------------- 숏로그 --------------
            case SHORLOG_LIKE,
                 SHORLOG_BOOKMARK ->
                    "/shorlog/" + targetId;

            case SHORLOG_COMMENT ->
                    "/shorlog/" + targetId + "?focus=comment";

            // -------------- 팔로우 --------------
            case FOLLOW ->
                    "/profile/" + targetId;
        };
    }
}
