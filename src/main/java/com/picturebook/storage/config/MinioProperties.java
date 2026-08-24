package com.picturebook.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MinIO 설정 바인딩
 * <p>
 * endpoint: 백엔드 → MinIO 직접 통신용 (예: http://localhost:9000)
 * publicEndpoint: 브라우저 POST 대상 및 public image URL에 사용하는 외부 노출 주소
 */
@ConfigurationProperties(prefix = "minio")
public record MinioProperties(
        String endpoint,
        String publicEndpoint,
        String accessKey,
        String secretKey,
        String bucket
) {
}
