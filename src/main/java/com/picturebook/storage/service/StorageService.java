package com.picturebook.storage.service;

import java.io.InputStream;

/**
 * 오브젝트 스토리지 추상화.
 * 현재 구현체는 MinIO이며, 추후 S3 등으로 교체 가능하도록 벤더 비의존적으로 설계.
 */
public interface StorageService {

    /**
     * 객체 업로드.
     *
     * @param objectKey   버킷 내 경로 (예: "books/{bookId}/cover.png")
     * @param inputStream 업로드할 바이트 스트림
     * @param size        스트림 크기 (바이트)
     * @param contentType MIME 타입 (예: "image/png")
     * @return 프론트엔드에서 접근 가능한 공개 URL
     */
    String upload(String objectKey, InputStream inputStream, long size, String contentType);

    /**
     * 객체 삭제.
     */
    void delete(String objectKey);

    /**
     * 객체의 공개 URL 반환.
     */
    String getPublicUrl(String objectKey);
}
