package com.picturebook.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "유료 출판 요청")
public record PaidPublishRequest(
    @Schema(description = "판매 가격(원)", example = "3000")
    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 1, message = "가격은 1 이상이어야 합니다.")
    Integer price
) {
}
