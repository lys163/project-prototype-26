package com.picturebook.storage.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.picturebook.storage.config.MinioProperties;

class PublicImageUrlPolicyTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final String OBJECT = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa.png";

    private final PublicImageUrlPolicy policy = new PublicImageUrlPolicy(
            new MinioProperties("http://minio.internal:9000", "https://images.example.test", "ignored", "ignored", "assets"));

    @Test
    void allowsOwnStorageUrl() {
        assertTrue(policy.isCurrentUserImageUrl(urlFor(USER_ID), USER_ID));
    }

    @Test
    void rejectsOtherUserAndExternalUrls() {
        assertFalse(policy.isCurrentUserImageUrl(urlFor(OTHER_USER_ID), USER_ID));
        assertFalse(policy.isCurrentUserImageUrl("https://external.example/avatar.png", USER_ID));
    }

    @Test
    void rejectsMalformedOrSignedUrlsAndUnexpectedExtensions() {
        assertFalse(policy.isCurrentUserImageUrl("not a uri", USER_ID));
        assertFalse(policy.isCurrentUserImageUrl(urlFor(USER_ID) + "?signature=value", USER_ID));
        assertFalse(policy.isCurrentUserImageUrl(urlFor(USER_ID).replace(".png", ".svg"), USER_ID));
    }

    @Test
    void preservesExistingNullAndEmptyContract() {
        assertTrue(policy.isCurrentUserImageUrl(null, USER_ID));
        assertTrue(policy.isCurrentUserImageUrl("", USER_ID));
    }

    private static String urlFor(UUID userId) {
        return "https://images.example.test/assets/users/" + userId + "/" + OBJECT;
    }
}
