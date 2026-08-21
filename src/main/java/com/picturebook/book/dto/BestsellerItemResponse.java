package com.picturebook.book.dto;

import java.util.UUID;

public record BestsellerItemResponse(
    UUID bookId,
    String title,
    // 책 표지 이미지 URL
    String coverImageUrl,
    // 작가 이름
    String authorName,
    // 판매 건수
    Long salesCount
) {
}
