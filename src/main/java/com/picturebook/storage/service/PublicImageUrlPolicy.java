package com.picturebook.storage.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.picturebook.storage.config.MinioProperties;

/**
 * 프로필에 새로 저장할 public image URL이 현재 사용자의 storage namespace에 속하는지 검사합니다.
 */
@Component
public class PublicImageUrlPolicy {

    private static final Pattern OBJECT_NAME = Pattern.compile(
            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(?:jpg|png|webp)");

    private final URI publicEndpoint;
    private final String bucket;

    public PublicImageUrlPolicy(MinioProperties properties) {
        String endpoint = properties.publicEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            endpoint = properties.endpoint();
        }
        try {
            this.publicEndpoint = new URI(endpoint).normalize();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("MinIO public endpoint 형식이 올바르지 않습니다.", e);
        }
        this.bucket = properties.bucket();
    }

    public boolean isCurrentUserImageUrl(String value, UUID userId) {
        if (value == null || value.isBlank()) {
            return true;
        }

        try {
            URI candidate = new URI(value).normalize();
            if (candidate.getRawQuery() != null || candidate.getRawFragment() != null || candidate.getRawUserInfo() != null) {
                return false;
            }
            if (!sameOrigin(candidate, publicEndpoint)) {
                return false;
            }

            String expectedPrefix = normalizedPath(publicEndpoint.getRawPath())
                    + "/" + bucket + "/users/" + userId + "/";
            String rawPath = candidate.getRawPath();
            if (rawPath == null || !rawPath.startsWith(expectedPrefix)) {
                return false;
            }

            String objectName = rawPath.substring(expectedPrefix.length());
            return OBJECT_NAME.matcher(objectName).matches();
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private static boolean sameOrigin(URI left, URI right) {
        return equalsIgnoreCase(left.getScheme(), right.getScheme())
                && equalsIgnoreCase(left.getHost(), right.getHost())
                && effectivePort(left) == effectivePort(right);
    }

    private static int effectivePort(URI uri) {
        if (uri.getPort() != -1) {
            return uri.getPort();
        }
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }

    private static String normalizedPath(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            return "";
        }
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    private static boolean equalsIgnoreCase(String left, String right) {
        return left != null && right != null && left.toLowerCase(Locale.ROOT).equals(right.toLowerCase(Locale.ROOT));
    }
}
