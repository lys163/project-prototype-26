package com.picturebook.storage.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.picturebook.storage.dto.PresignedUploadRequest;
import com.picturebook.storage.dto.PresignedUrlResponse;
import com.picturebook.storage.port.ObjectStoragePort;

import lombok.RequiredArgsConstructor;

/**
 * 오브젝트 스토리지 관련 유스케이스
 * <p>
 * 프리사인드 URL 발급 흐름:
 * 1. 클라이언트 → 백엔드: 업로드 URL 요청
 * 2. 백엔드: objectKey 생성 + MinIO에서 프리사인드 URL 발급
 * 3. 클라이언트 → MinIO: PUT 요청으로 직접 업로드 (백엔드 경유 X)
 * 4. 이후 다운로드 시에도 필요하면 다운로드 URL 발급
 */
@Service
@RequiredArgsConstructor
public class StorageUseCase {

    private static final Duration UPLOAD_URL_EXPIRY = Duration.ofMinutes(10);
    private static final Duration DOWNLOAD_URL_EXPIRY = Duration.ofHours(1);

    private final ObjectStoragePort objectStoragePort;

    /**
     * 업로드용 프리사인드 URL 발급
     *
     * @param userId  현재 로그인 사용자 (object key 네임스페이스 구분용)
     * @param request 파일 메타 정보
     */
    public PresignedUrlResponse issueUploadUrl(UUID userId, PresignedUploadRequest request) {
        String objectKey = buildObjectKey(userId, request.filename());
        String url = objectStoragePort.presignedUploadUrl(objectKey, UPLOAD_URL_EXPIRY);
        return new PresignedUrlResponse(objectKey, url, stripQueryParams(url), UPLOAD_URL_EXPIRY.getSeconds());
    }

    /**
     * 다운로드용 프리사인드 URL 발급
     *
     * @param objectKey DB에 저장해둔 object key
     */
    public PresignedUrlResponse issueDownloadUrl(String objectKey) {
        String url = objectStoragePort.presignedDownloadUrl(objectKey, DOWNLOAD_URL_EXPIRY);
        return new PresignedUrlResponse(objectKey, url, stripQueryParams(url), DOWNLOAD_URL_EXPIRY.getSeconds());
    }

    /**
     * 프리사인드 URL에서 쿼리 파라미터(서명 부분)를 제거한 public URL 반환.
     * 버킷이 public read로 열려있을 때 DB에 저장해 영구 링크로 사용하기 위함.
     */
    private String stripQueryParams(String url) {
        int queryIndex = url.indexOf('?');
        return queryIndex < 0 ? url : url.substring(0, queryIndex);
    }

    /**
     * 오브젝트 키 생성: users/{userId}/{uuid}.{ext}
     * <p>
     * 사용자별 네임스페이스로 분리. 파일명 충돌 방지를 위해 UUID로 재작성. 확장자는 원본 보존.
     */
    private String buildObjectKey(UUID userId, String filename) {
        String extension = extractExtension(filename);
        String uniqueName = UUID.randomUUID() + extension;
        return "users/" + userId + "/" + uniqueName;
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex <= 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex).toLowerCase();
    }
}
