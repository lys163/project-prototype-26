package com.picturebook.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "리뷰 생성/수정 요청")
public record ReviewRequest(

        @Schema(description = "별점", example = "5")
        @NotNull(message = "별점은 필수입니다.")
        @Min(value = 1, message = "별점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "별점은 5점 이하여야 합니다.")
        Integer rating,

        @Schema(description = "리뷰 내용", example = "아이와 함께 읽기 좋은 그림책이에요.")
        @Size(max = 500, message = "리뷰 내용은 500자 이내여야 합니다.")
        String content
) {
}
