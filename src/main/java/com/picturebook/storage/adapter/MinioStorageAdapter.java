package com.picturebook.storage.adapter;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.picturebook.storage.port.ObjectStoragePort;
import com.picturebook.storage.config.MinioProperties;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MinioStorageAdapter implements ObjectStoragePort {

    private final MinioClient minioClient;
    private final MinioClient publicMinioClient;
    private final String bucket;

    public MinioStorageAdapter(
            MinioClient minioClient,
            @Qualifier("publicMinioClient") MinioClient publicMinioClient,
            MinioProperties properties
    ) {
        this.minioClient = minioClient;
        this.publicMinioClient = publicMinioClient;
        this.bucket = properties.bucket();
    }

    @Override
    public String presignedUploadUrl(String objectKey, Duration expiry) {
        return generatePresignedUrl(Method.PUT, objectKey, expiry);
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
}
