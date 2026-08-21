package com.picturebook.storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프리사인드 URL 응답")
public record PresignedUrlResponse(
        @Schema(description = "버킷 내 오브젝트 키", example = "users/abc123/1717689600000.png")
        String objectKey,

        @Schema(description = "프리사인드 URL (PUT/GET 용도, 쿼리 파라미터 포함)", example = "https://img.mongle.cloud/picturebook/users/abc123/1717689600000.png?X-Amz-...")
        String url,

        @Schema(description = "쿼리 파라미터 제거한 public URL (DB 저장용, 버킷이 public read여야 함)", example = "https://img.mongle.cloud/picturebook/users/abc123/1717689600000.png")
        String publicUrl,

        @Schema(description = "URL 유효 기간 (초)", example = "600")
        long expiresInSeconds
) {
}
