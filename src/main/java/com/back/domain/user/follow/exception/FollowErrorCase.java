package com.back.domain.user.follow.exception;

import com.back.global.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FollowErrorCase implements ErrorCase {
    CANNOT_FOLLOW_YOURSELF(HttpStatus.BAD_REQUEST, 2001, "자기 자신을 팔로우할 수 없습니다."),
    ALREADY_FOLLOWING(HttpStatus.BAD_REQUEST, 2002, "이미 팔로우 중인 사용자입니다."),
    NOT_EXISTING_FOLLOW(HttpStatus.BAD_REQUEST, 2003, "이미 팔로우가 되어 있지 않습니다.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
}
