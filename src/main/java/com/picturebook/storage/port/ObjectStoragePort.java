package com.picturebook.storage.port;

import java.time.Duration;

/**
 * 오브젝트 스토리지 포트
 * <p>
 * 스토리지 인터페이스. 구현체는 MinioStorageAdapter에서 제공.
 * 외부 프레임워크/SDK 의존성 없음.
 */
public interface ObjectStoragePort {

    /**
     * 클라이언트가 multipart/form-data POST로 직접 업로드할 수 있는 서명된 form 계약을 발급합니다.
     */
    PresignedPost presignedUploadPost(
            String objectKey,
            String contentType,
            long minSize,
            long maxSize,
            Duration expiry
    );

    String publicObjectUrl(String objectKey);

    /**
     * 클라이언트가 GET 요청으로 파일을 다운로드할 수 있는 프리사인드 URL 발급
     *
     * @param objectKey 버킷 내 오브젝트 키
     * @param expiry    URL 유효 기간
     * @return 프리사인드 다운로드 URL
     */
    String presignedDownloadUrl(String objectKey, Duration expiry);

    /**
     * 오브젝트 삭제
     *
     * @param objectKey 버킷 내 오브젝트 키
     */
    void deleteObject(String objectKey);
}
