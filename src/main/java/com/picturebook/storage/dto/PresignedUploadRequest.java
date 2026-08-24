package com.picturebook.storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "프리사인드 POST form 발급 요청")
public record PresignedUploadRequest(
        @Schema(description = "원본 파일명 (확장자 추출용)", example = "cat.png")
        @NotBlank String filename,

        @Schema(description = "MIME 타입", example = "image/png")
        @NotBlank String contentType,

        @Schema(description = "업로드할 파일 크기(byte)", example = "1048576")
        @NotNull @Positive @Max(5 * 1024 * 1024) Long fileSize
) {
}
