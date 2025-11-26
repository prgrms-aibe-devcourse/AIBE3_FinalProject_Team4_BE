package com.back.domain.notification.exception;

import com.back.global.exception.ErrorCase;
import com.back.global.rsData.RsData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCase implements ErrorCase {

    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, 8001, "알림을 찾을 수 없습니다."),
    NOTIFICATION_FORBIDDEN(HttpStatus.FORBIDDEN, 8002, "본인의 알림만 처리할 수 있습니다.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    @Override
    public RsData<?> toRsData() {
        return ErrorCase.super.toRsData();
    }
}
