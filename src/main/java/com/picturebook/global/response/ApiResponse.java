package com.picturebook.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공통 API 응답 래퍼
 * <p>
 * 성공 시: status, success=true, data에 결과, error는 직렬화에서 제외
 * 실패 시: status, success=false, error에 에러 정보, data는 직렬화에서 제외
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int status;
    private boolean success;
    private T data;
    private ErrorDetail error;

    // ── 성공 응답 ──

    public static <T> ApiResponse<T> ok(int status,T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.status = status;
        response.success = true;
        response.data = data;
        return response;
    }

    public static ApiResponse<Void> ok(int status) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.status = status;
        response.success = true;
        return response;
    }

    // ── 실패 응답 ──

    public static ApiResponse<Void> fail(int status, String code, String message) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.status = status;
        response.success = false;
        response.error = new ErrorDetail(code, message);
        return response;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ErrorDetail {
        private String code;
        private String message;

        private ErrorDetail(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
