package com.picturebook.user.dto;

public record AuthorSummaryResponse(
    // 총 수익
    Long totalRevenue,
    // 이번달 수익
    Long monthlyRevenue,
    // 총 출간 수
    Long publishedBookCount,
    // 현재 작성 중인 책 수
    Long inProgressBookCount,
    // 총 조회 수
    Long totalViewCount,
    // 평균 책 평점
    Double averageBookRating
) {
}
