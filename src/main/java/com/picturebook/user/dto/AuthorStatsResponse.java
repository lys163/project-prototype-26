package com.picturebook.user.dto;

public record AuthorStatsResponse(
    // 작가 작품 수 (공개/링크공유 책)
    long bookCount,
    // 총 좋아요 수
    long totalLikeCount,
    // 평균 평점 (소수 1자리)
    double averageRating,
    // 팔로워 수
    long followerCount
) {
}
