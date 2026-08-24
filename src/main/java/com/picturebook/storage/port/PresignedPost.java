package com.picturebook.storage.port;

import java.util.Map;

/**
 * 브라우저가 object storage에 직접 multipart/form-data POST를 전송하기 위한 서명된 계약입니다.
 */
public record PresignedPost(
        String uploadUrl,
        Map<String, String> formFields
) {
}
