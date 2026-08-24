package com.picturebook.storage.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.picturebook.storage.config.MinioProperties;
import com.picturebook.storage.port.PresignedPost;

import io.minio.MinioClient;

class MinioStorageAdapterTest {

    @Test
    void generatesSignedPostFieldsWithExactKeyMimeAndSizeRange() {
        MinioProperties properties = new MinioProperties(
                "http://minio.internal:9000",
                "https://images.example.test",
                "test-access-key",
                "test-secret-key",
                "assets");
        MinioClient internalClient = client(properties.endpoint(), properties);
        MinioClient publicClient = client(properties.publicEndpoint(), properties);
        MinioStorageAdapter adapter = new MinioStorageAdapter(internalClient, publicClient, properties);

        PresignedPost post = adapter.presignedUploadPost(
                "users/user-id/object.png",
                "image/png",
                1,
                5L * 1024 * 1024,
                Duration.ofMinutes(10));

        assertEquals("https://images.example.test/assets", post.uploadUrl());
        assertEquals("users/user-id/object.png", post.formFields().get("key"));
        assertEquals("image/png", post.formFields().get("Content-Type"));

        String policyJson = new String(
                Base64.getDecoder().decode(post.formFields().get("policy")), StandardCharsets.UTF_8);
        assertTrue(policyJson.contains("[\"content-length-range\",1,5242880]"));
        assertTrue(policyJson.contains("\"$key\",\"users/user-id/object.png\""));
        assertTrue(policyJson.contains("\"$Content-Type\",\"image/png\""));
    }

    private static MinioClient client(String endpoint, MinioProperties properties) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(properties.accessKey(), properties.secretKey())
                .region("us-east-1")
                .build();
    }
}
