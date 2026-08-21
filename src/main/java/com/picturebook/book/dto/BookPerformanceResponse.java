package com.picturebook.book.dto;

import java.util.UUID;

import com.picturebook.book.enums.VisibilityType;

public record BookPerformanceResponse(
    UUID bookId,
    String title,
    VisibilityType visibility,
    Long viewCount,
    Long likeCount,
    Double averageRating,
    Long totalRevenue
) {
}
