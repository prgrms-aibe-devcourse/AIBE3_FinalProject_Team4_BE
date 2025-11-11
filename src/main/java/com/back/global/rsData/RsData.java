package com.back.global.rsData;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.lang.NonNull;

/**
 * API 응답을 통일된 형식으로 관리하기 위한 Record 클래스
 *
 * @param resultCode 상태 코드와 세부 코드 조합 (예: "200-1", "400-1")
 * @param statusCode HTTP 상태 코드 (resultCode에서 추출)
 * @param msg        사용자에게 보여줄 메시지
 * @param data       실제 데이터 payload (성공 시 객체, 실패 시 null 가능)
 */
public record RsData<T>(
        @NonNull String resultCode,
        @JsonIgnore int statusCode, // JSON 직렬화에서는 제외
        @NonNull String msg,
        T data) {

    /**
     * 메시지만 있는 생성자
     * @param resultCode 코드 (ex: "400-1")
     * @param msg 메시지
     */
    public RsData(String resultCode, String msg) {
        this(resultCode, parseStatusCode(resultCode), msg, null);
    }

    /**
     * 데이터까지 포함하는 생성자
     * @param resultCode 코드 (ex: "200-1")
     * @param msg 메시지
     * @param data 실제 응답 데이터
     */
    public RsData(String resultCode, String msg, T data) {
        this(resultCode, parseStatusCode(resultCode), msg, data);
    }

    /**
     * resultCode 문자열에서 HTTP 상태 코드 추출
     * 예: "200-1" → 200, "400-3" → 400
     * 실패 시 기본값 400
     */
    private static int parseStatusCode(String resultCode) {
        try {
            return Integer.parseInt(resultCode.split("-", 2)[0]);
        } catch (Exception e) {
            return 400;
        }
    }

    /**
     * 지정한 코드, 메시지, 데이터로 RsData 생성
     */
    public static <T> RsData<T> of(String resultCode, String msg, T data) {
        return new RsData<>(resultCode, msg, data);
    }

    /**
     * 성공 응답 생성
     * HTTP 200, 기본 메시지 "성공", 데이터 포함
     */
    public static <T> RsData<T> successOf(T data) {
        return of("200-1", "성공", data);
    }

    /**
     * 실패 응답 생성 (단순 메시지)
     * HTTP 400, 데이터는 null
     */
    public static <T> RsData<T> failOf(String msg) {
        return of("400-1", msg, null);
    }

    /**
     * 실패 응답 생성 (코드 + 메시지 지정 가능)
     * ex: failOf("403-1", "권한이 없습니다.")
     */
    public static <T> RsData<T> failOf(String resultCode, String msg) {
        return of(resultCode, msg, null);
    }
}
