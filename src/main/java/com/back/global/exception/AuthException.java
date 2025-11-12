package com.back.global.exception;

import com.back.global.rsData.RsData;

public class AuthException extends RuntimeException{
    private final String resultCode;
    private final String msg;

    public AuthException(String resultCode, String msg) {
        super(resultCode + ":" + msg);
        this.resultCode = resultCode;
        this.msg = msg;
    }

    public RsData<Void> getRsData() {
        return new RsData<>(resultCode, msg, null);
    }
}
