package com.picturebook.global.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * Swagger 문서 전용 에러 응답 스키마
 * <p>
 * 실제 직렬화는 {@link ApiResponse#fail} 이 담당한다. 이 클래스는 4xx·5xx 응답 예시를
 * 성공 응답(data)과 분리해서 보여주기 위한 문서용 모델이다.
 */
@Getter
@Schema(name = "ErrorResponse", description = "에러 응답")
public class ErrorResponse {

    @Schema(description = "HTTP 상태 코드", example = "404")
    private int status;

    @Schema(description = "성공 여부 (에러는 항상 false)", example = "false")
    private boolean success;

    private ErrorDetail error;

    @Getter
    @Schema(name = "ErrorDetail", description = "에러 상세")
    public static class ErrorDetail {

        @Schema(description = "에러 코드", example = "BOOK_001")
        private String code;

        @Schema(description = "에러 메시지", example = "해당 도서를 찾을 수 없습니다.")
        private String message;
    }
}
