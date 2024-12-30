package com.nowait.controller.api.dto.response;

import org.springframework.http.HttpStatus;

/**
 * API 응답을 표준화하기 위한 제네릭 레코드(Record)
 * <p>
 * 이 클래스는 API 응답에서 반환되는 상태 코드, 상태 메시지, 응답 데이터 등을 포함하여
 * 일관된 응답 형식을 제공하기 위해 설계되었습니다.
 */
public record ApiResult<T>(
    int code,
    HttpStatus status,
    String message,
    T data
) {

    private ApiResult(HttpStatus status, String message, T data) {
        this(status.value(), status, message, data);
    }

    public static <T> ApiResult<T> of(HttpStatus httpStatus, String message, T data) {
        return new ApiResult<>(httpStatus, message, data);
    }

    public static <T> ApiResult<T> of(HttpStatus httpStatus, T data) {
        return of(httpStatus, httpStatus.name(), data);
    }

    public static ApiResult<Void> of(HttpStatus httpStatus) {
        return new ApiResult<>(httpStatus, httpStatus.name(), null);
    }

    public static <T> ApiResult<T> ok(T data) {
        return of(HttpStatus.OK, data);
    }

    public static <T> ApiResult<T> error(HttpStatus httpStatus, String message) {
        return new ApiResult<>(httpStatus, message, null);
    }
}
