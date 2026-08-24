package com.picturebook.storage.dto;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프리사인드 업로드 form 응답")
public record PresignedUrlResponse(
        @Schema(description = "버킷 내 오브젝트 키", example = "users/abc123/1717689600000.png")
        String objectKey,

        @Schema(description = "multipart/form-data POST 대상 URL")
        String uploadUrl,

        @Schema(description = "업로드 시 그대로 전송해야 하는 서명된 form fields")
        Map<String, String> fields,

        @Schema(description = "DB 저장 및 이미지 조회에 사용하는 permanent public URL", example = "https://img.mongle.cloud/picturebook/users/abc123/1717689600000.png")
        String publicUrl,

        @Schema(description = "URL 유효 기간 (초)", example = "600")
        long expiresInSeconds
) {
}
