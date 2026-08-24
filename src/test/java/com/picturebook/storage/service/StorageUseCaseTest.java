package com.picturebook.storage.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.picturebook.global.exception.CustomException;
import com.picturebook.global.exception.ErrorCode;
import com.picturebook.storage.dto.PresignedUploadRequest;
import com.picturebook.storage.dto.PresignedUrlResponse;
import com.picturebook.storage.port.ObjectStoragePort;
import com.picturebook.storage.port.PresignedPost;

@ExtendWith(MockitoExtension.class)
class StorageUseCaseTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final long MAX_SIZE = 5L * 1024 * 1024;

    @Mock
    private ObjectStoragePort objectStoragePort;

    private StorageUseCase storageUseCase;

    @BeforeEach
    void setUp() {
        storageUseCase = new StorageUseCase(objectStoragePort);
    }

    @ParameterizedTest
    @ValueSource(strings = {"image/jpeg", "image/png", "image/webp"})
    void issuesPostPolicyForAllowedImageTypes(String contentType) {
        when(objectStoragePort.presignedUploadPost(any(), eq(contentType), eq(1L), eq(MAX_SIZE), eq(Duration.ofMinutes(10))))
                .thenAnswer(invocation -> new PresignedPost(
                        "https://images.example.test/assets",
                        Map.of("key", invocation.getArgument(0, String.class), "Content-Type", contentType)));
        when(objectStoragePort.publicObjectUrl(any())).thenAnswer(invocation ->
                "https://images.example.test/assets/" + invocation.getArgument(0, String.class));

        PresignedUrlResponse response = storageUseCase.issueUploadUrl(
                USER_ID, new PresignedUploadRequest("untrusted-name.html", contentType, MAX_SIZE));

        assertTrue(response.objectKey().matches("users/" + USER_ID + "/[0-9a-f-]+\\.(jpg|png|webp)"));
        assertEquals("https://images.example.test/assets", response.uploadUrl());
        assertEquals(contentType, response.fields().get("Content-Type"));
        assertEquals(600L, response.expiresInSeconds());
        verify(objectStoragePort).presignedUploadPost(
                response.objectKey(), contentType, 1L, MAX_SIZE, Duration.ofMinutes(10));
    }

    @ParameterizedTest
    @ValueSource(strings = {"image/gif", "image/svg+xml", "application/octet-stream", "text/html", ""})
    void rejectsUnsupportedImageTypes(String contentType) {
        CustomException exception = assertThrows(CustomException.class, () -> storageUseCase.issueUploadUrl(
                USER_ID, new PresignedUploadRequest("image.png", contentType, 1L)));

        assertEquals(ErrorCode.UNSUPPORTED_IMAGE_TYPE, exception.getErrorCode());
    }

    @Test
    void rejectsZeroAndNegativeSizes() {
        for (long size : new long[] {0L, -1L}) {
            CustomException exception = assertThrows(CustomException.class, () -> storageUseCase.issueUploadUrl(
                    USER_ID, new PresignedUploadRequest("image.png", "image/png", size)));
            assertEquals(ErrorCode.INVALID_IMAGE_SIZE, exception.getErrorCode());
        }
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 1048576L, 5242880L})
    void allowsSizeBoundariesAndNormalSize(long fileSize) {
        when(objectStoragePort.presignedUploadPost(any(), eq("image/png"), eq(1L), eq(MAX_SIZE), eq(Duration.ofMinutes(10))))
                .thenAnswer(invocation -> new PresignedPost(
                        "https://images.example.test/assets", Map.of("key", invocation.getArgument(0, String.class))));
        when(objectStoragePort.publicObjectUrl(any())).thenReturn("https://images.example.test/assets/object.png");

        storageUseCase.issueUploadUrl(USER_ID, new PresignedUploadRequest("image.png", "image/png", fileSize));
    }

    @Test
    void rejectsSizeOverFiveMiB() {
        CustomException exception = assertThrows(CustomException.class, () -> storageUseCase.issueUploadUrl(
                USER_ID, new PresignedUploadRequest("image.png", "image/png", MAX_SIZE + 1)));

        assertEquals(ErrorCode.INVALID_IMAGE_SIZE, exception.getErrorCode());
    }
}
