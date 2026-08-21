package com.picturebook.reading.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "책 읽기 진행 상황 업데이트 요청")
public record ReadingPageRequest(
    @Schema(description = "마지막으로 읽은 페이지 번호")
    @NotNull(message = "페이지 번호는 필수입니다.")
    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
    Integer lastReadPageNumber
) {
} 
