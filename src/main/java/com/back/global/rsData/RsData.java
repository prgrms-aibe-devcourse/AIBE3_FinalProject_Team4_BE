package com.back.global.rsData;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.lang.NonNull;

public record RsData<T>(
        @NonNull String resultCode,
        @JsonIgnore int statusCode,
        @NonNull String msg,
        @NonNull T data) {
    public RsData(String resultCode, String msg) {
        this(resultCode, msg, null);
    }

    public RsData(String resultCode, String msg, T data) {
        this(resultCode, Integer.parseInt(resultCode.split("-", 2)[0]), msg, data);
    }

    public static <T> RsData<T> of(String resultCode, String msg, T data) {
        return new RsData<>(resultCode, msg, data);
    }

    public static <T> RsData<T> successOf(T data) {
        return of("200-1", "성공", data);
    }

    public static <T> RsData<T> failOf(String msg) {
        return of("400-1", msg, null);
    }
}