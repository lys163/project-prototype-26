package com.picturebook.like.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record LikedBookResponse(
        UUID bookId,
        String title,
        String coverImageUrl,
        String authorName,
        LocalDateTime likedAt,
        boolean liked
) {
}
