package com.back.global.exception;

import com.back.global.rsData.RsData;
import lombok.Getter;

/**
 * 서비스 계층에서 발생하는 예외를 래핑하는 커스텀 예외 클래스
 */
@Getter
public class ServiceException extends RuntimeException {

    private final ErrorCase errorCase;

    public ServiceException(ErrorCase errorCase) {
        super(errorCase.getMessage());
        this.errorCase = errorCase;
    }

    public ServiceException(ErrorCase errorCase, Throwable cause) {
        super(errorCase.getMessage(), cause);
        this.errorCase = errorCase;
    }

    /**
     * ✅ RsData로 바로 변환 (Controller에서 바로 응답 가능)
     */
    public RsData<?> toRsData() {
        return RsData.failOf(
                errorCase.getHttpStatusCode() + "-" + errorCase.getCode(),
                errorCase.getMessage()
        );
    }
}
