package com.picturebook.review.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 삭제 응답")
public record ReviewDeleteResponse(
        @Schema(description = "삭제된 리뷰 ID")
        UUID reviewId,

        @Schema(description = "삭제된 리뷰가 속한 책 ID")
        UUID bookId,

        @Schema(description = "삭제 성공 여부")
        boolean deleted
) {
}
