package com.picturebook.storage.adapter;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.picturebook.storage.port.ObjectStoragePort;
import com.picturebook.storage.port.PresignedPost;
import com.picturebook.storage.config.MinioProperties;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PostPolicy;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MinioStorageAdapter implements ObjectStoragePort {

    private final MinioClient minioClient;
    private final MinioClient publicMinioClient;
    private final String bucket;
    private final String publicEndpoint;

    public MinioStorageAdapter(
            MinioClient minioClient,
            @Qualifier("publicMinioClient") MinioClient publicMinioClient,
            MinioProperties properties
    ) {
        this.minioClient = minioClient;
        this.publicMinioClient = publicMinioClient;
        this.bucket = properties.bucket();
        this.publicEndpoint = effectivePublicEndpoint(properties);
    }

    @Override
    public PresignedPost presignedUploadPost(
            String objectKey,
            String contentType,
            long minSize,
            long maxSize,
            Duration expiry
    ) {
        try {
            PostPolicy policy = new PostPolicy(bucket, ZonedDateTime.now().plus(expiry));
            policy.addEqualsCondition("key", objectKey);
            policy.addEqualsCondition("Content-Type", contentType);
            policy.addContentLengthRangeCondition(minSize, maxSize);
            Map<String, String> formFields = new HashMap<>(publicMinioClient.getPresignedPostFormData(policy));
            formFields.put("key", objectKey);
            formFields.put("Content-Type", contentType);
            return new PresignedPost(uploadTargetUrl(), Map.copyOf(formFields));
        } catch (Exception e) {
            log.error("프리사인드 POST 정책 생성 실패: bucket={}, key={}", bucket, objectKey, e);
            throw new RuntimeException("프리사인드 POST 정책 생성에 실패했습니다.", e);
        }
    }

    @Override
    public String presignedDownloadUrl(String objectKey, Duration expiry) {
        return generatePresignedUrl(Method.GET, objectKey, expiry);
    }

    @Override
    public void deleteObject(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            log.error("MinIO 오브젝트 삭제 실패: bucket={}, key={}", bucket, objectKey, e);
            throw new RuntimeException("오브젝트 삭제에 실패했습니다.", e);
        }
    }

    @Override
    public String publicObjectUrl(String objectKey) {
        return uploadTargetUrl() + "/" + objectKey;
    }

    private String generatePresignedUrl(Method method, String objectKey, Duration expiry) {
        try {
            return publicMinioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(method)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry((int) expiry.getSeconds(), TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("프리사인드 URL 생성 실패: method={}, bucket={}, key={}", method, bucket, objectKey, e);
            throw new RuntimeException("프리사인드 URL 생성에 실패했습니다.", e);
        }
    }

    private String uploadTargetUrl() {
        return stripTrailingSlash(publicEndpoint) + "/" + bucket;
    }

    private static String effectivePublicEndpoint(MinioProperties properties) {
        String endpoint = properties.publicEndpoint();
        return endpoint == null || endpoint.isBlank() ? properties.endpoint() : endpoint;
    }

    private static String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
