package com.picturebook.storage.service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.picturebook.storage.dto.PresignedUploadRequest;
import com.picturebook.storage.dto.PresignedUrlResponse;
import com.picturebook.storage.port.ObjectStoragePort;
import com.picturebook.storage.port.PresignedPost;
import com.picturebook.global.exception.CustomException;
import com.picturebook.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 오브젝트 스토리지 관련 유스케이스
 * <p>
 * 프리사인드 URL 발급 흐름:
 * 1. 클라이언트 → 백엔드: 업로드 URL 요청
 * 2. 백엔드: objectKey 생성 + MinIO POST policy 발급
 * 3. 클라이언트 → MinIO: multipart/form-data POST로 직접 업로드 (백엔드 경유 X)
 */
@Service
@RequiredArgsConstructor
public class StorageUseCase {

    private static final Duration UPLOAD_URL_EXPIRY = Duration.ofMinutes(10);
    private static final long MAX_UPLOAD_SIZE = 5L * 1024 * 1024;
    private static final long MIN_UPLOAD_SIZE = 1L;

    private final ObjectStoragePort objectStoragePort;

    /**
     * 업로드용 프리사인드 URL 발급
     *
     * @param userId  현재 로그인 사용자 (object key 네임스페이스 구분용)
     * @param request 파일 메타 정보
     */
    public PresignedUrlResponse issueUploadUrl(UUID userId, PresignedUploadRequest request) {
        if (request == null || request.filename() == null || request.filename().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        String extension = extensionFor(request.contentType());
        validateSize(request.fileSize());
        String objectKey = buildObjectKey(userId, extension);
        PresignedPost post = objectStoragePort.presignedUploadPost(
                objectKey,
                request.contentType(),
                MIN_UPLOAD_SIZE,
                MAX_UPLOAD_SIZE,
                UPLOAD_URL_EXPIRY);
        return new PresignedUrlResponse(
                objectKey,
                post.uploadUrl(),
                Map.copyOf(post.formFields()),
                objectStoragePort.publicObjectUrl(objectKey),
                UPLOAD_URL_EXPIRY.getSeconds());
    }

    /**
     * 오브젝트 키 생성: users/{userId}/{uuid}.{ext}
     * <p>
     * 사용자별 네임스페이스로 분리하고 파일명 충돌을 UUID로 방지합니다.
     * 확장자는 caller filename이 아니라 검증된 MIME 정책에서 결정합니다.
     */
    private String buildObjectKey(UUID userId, String extension) {
        String uniqueName = UUID.randomUUID() + extension;
        return "users/" + userId + "/" + uniqueName;
    }

    private String extensionFor(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            throw new CustomException(ErrorCode.UNSUPPORTED_IMAGE_TYPE);
        }
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new CustomException(ErrorCode.UNSUPPORTED_IMAGE_TYPE);
        };
    }

    private void validateSize(Long fileSize) {
        if (fileSize == null || fileSize < MIN_UPLOAD_SIZE || fileSize > MAX_UPLOAD_SIZE) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_SIZE);
        }
    }
}
