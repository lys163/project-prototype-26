package com.picturebook.storage.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.public-endpoint}")
    private String publicEndpoint;

    /**
     * 앱 시작 시 버킷이 없으면 생성하고 public-read 정책을 부여한다.
     * 이미지는 프론트엔드에서 직접 GET 해야 하므로 public-read가 필요하다.
     */
    @PostConstruct
    public void init() {
        try {
            BucketExistsArgs existsArgs = BucketExistsArgs.builder().bucket(bucket).build();

            if (!minioClient.bucketExists(existsArgs)) {
                try {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                    log.info("MinIO 버킷 생성됨: {}", bucket);
                } catch (Exception createEx) {
                    // 동시 기동(롤링 배포/스케일 아웃) 시 다른 인스턴스가 먼저 생성했을 수 있음.
                    // 재확인해서 존재하면 무시, 여전히 없으면 원래 예외 전파.
                    if (!minioClient.bucketExists(existsArgs)) {
                        throw createEx;
                    }
                    log.info("MinIO 버킷이 이미 존재함 (동시 생성 감지): {}", bucket);
                }
            }

            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucket)
                            .config(publicReadPolicy(bucket))
                            .build());
            log.info("MinIO 버킷 public-read 정책 적용됨: {}", bucket);
        } catch (Exception e) {
            throw new IllegalStateException("MinIO 버킷 초기화 실패: " + bucket, e);
        }
    }

    public String upload(String objectKey, InputStream inputStream, long size, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build());
            return getPublicUrl(objectKey);
        } catch (Exception e) {
            throw new IllegalStateException("MinIO 업로드 실패: " + objectKey, e);
        }
    }

    public void delete(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build());
        } catch (Exception e) {
            throw new IllegalStateException("MinIO 삭제 실패: " + objectKey, e);
        }
    }

    @Override
    public String getPublicUrl(String objectKey) {
        return String.format("%s/%s/%s",
                stripTrailingSlash(publicEndpoint),
                bucket,
                encodeObjectKey(objectKey));
    }

    private static String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * 오브젝트 키를 '/' 단위로 분리해 각 세그먼트를 RFC 3986에 맞게 인코딩한다.
     * 공백, 쉼표, '?', '#', 비ASCII 문자 등이 포함된 키도 안전하게 URL로 만들 수 있다.
     */
    private static String encodeObjectKey(String objectKey) {
        return Arrays.stream(objectKey.split("/"))
                .map(segment -> UriUtils.encodePathSegment(segment, StandardCharsets.UTF_8))
                .collect(Collectors.joining("/"));
    }

    private static String publicReadPolicy(String bucket) {
        return """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": {"AWS": ["*"]},
                      "Action": ["s3:GetObject"],
                      "Resource": ["arn:aws:s3:::%s/*"]
                    }
                  ]
                }
                """.formatted(bucket);
    }
}
