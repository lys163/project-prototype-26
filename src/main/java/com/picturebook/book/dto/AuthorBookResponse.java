package com.picturebook.book.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.picturebook.book.enums.VisibilityType;

public record AuthorBookResponse(
    UUID bookId,
    String title,
    String coverImageUrl,
    // 공개(PUBLIC) 또는 유료(PAID)
    VisibilityType visibility,
    // 가격 (null 또는 0이면 무료)
    Integer price,
    LocalDateTime publishedAt
) {
}
