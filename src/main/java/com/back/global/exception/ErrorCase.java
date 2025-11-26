package com.back.global.exception;

import com.back.global.rsData.RsData;
import org.springframework.http.HttpStatus;

/**
 * 모든 도메인별 ErrorCase Enum이 구현해야 하는 인터페이스
 */
public interface ErrorCase {

    HttpStatus getHttpStatus();

    int getCode();

    String getMessage();

    /**
     * ✅ 기본 HTTP 상태 코드 반환 (편의 메서드)
     */
    default int getHttpStatusCode() {
        return getHttpStatus().value();
    }

    /**
     * ✅ RsData 변환 헬퍼
     */
    default RsData<?> toRsData() {
        String resultCode = getHttpStatus().value() + "-" + getCode();
        return RsData.failOf(resultCode, getMessage());
    }
}
