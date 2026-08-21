package com.picturebook.review.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.picturebook.review.entity.Review;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 응답")
public record ReviewResponse(
        @Schema(description = "리뷰 ID")
        UUID reviewId,

        @Schema(description = "책 ID")
        UUID bookId,

        @Schema(description = "책 제목")
        String bookTitle,

        @Schema(description = "작성자 ID")
        UUID userId,

        @Schema(description = "작성자 닉네임")
        String nickname,

        @Schema(description = "별점")
        Integer rating,

        @Schema(description = "리뷰 내용")
        String content,

        @Schema(description = "현재 로그인 사용자가 작성한 리뷰 여부")
        boolean mine,

        @Schema(description = "생성 일시")
        LocalDateTime createdAt,

        @Schema(description = "수정 일시")
        LocalDateTime updatedAt
) {
    public static ReviewResponse from(Review review, String bookTitle, String nickname, UUID currentUserId) {
        return new ReviewResponse(
                review.getId(),
                review.getBookId(),
                bookTitle,
                review.getUserId(),
                nickname,
                review.getRating(),
                review.getContent(),
                currentUserId != null && currentUserId.equals(review.getUserId()),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
