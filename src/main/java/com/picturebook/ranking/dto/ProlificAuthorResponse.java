package com.picturebook.ranking.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "다작작가 랭킹 응답")
public record ProlificAuthorResponse(
    @Schema(description = "작가(사용자) 식별자", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID userId,

    @Schema(description = "작가 닉네임", example = "작가님")
    String nickname,

    @Schema(description = "작가 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    String profileImage,

    @Schema(description = "작가의 책 수", example = "5")
    Long bookCount,

    @Schema(description = "작가의 랭킹 순위", example = "1")
    int rank
) {
    public ProlificAuthorResponse withRank(int rank) {
        return new ProlificAuthorResponse(userId, nickname, profileImage, bookCount, rank);
    }
}
