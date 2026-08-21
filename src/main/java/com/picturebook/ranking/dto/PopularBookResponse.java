package com.picturebook.ranking.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인기 책 랭킹 응답")
public record PopularBookResponse(
    @Schema(description = "책 ID")
    UUID bookId,
    @Schema(description = "책 제목")
    String title,
    @Schema(description = "표지 이미지 URL")
    String coverImageUrl,
    @Schema(description = "저자 닉네임")
    String authorNickname,
    @Schema(description = "책 좋아요 수")
    Long likeCount,
    @Schema(description = "책 랭킹 순위")
    int rank
) {
    public PopularBookResponse withRank(int rank) {
        return new PopularBookResponse(bookId, title, coverImageUrl, authorNickname, likeCount, rank);
    } 
}
