package com.picturebook.storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "프리사인드 업로드 URL 발급 요청")
public record PresignedUploadRequest(
        @Schema(description = "원본 파일명 (확장자 추출용)", example = "cat.png")
        @NotBlank String filename,

        @Schema(description = "MIME 타입 — 클라이언트 PUT 시 동일 헤더 필요", example = "image/png")
        @NotBlank String contentType
) {
}
