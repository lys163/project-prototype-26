package com.picturebook.book.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.picturebook.book.enums.BookStatus;
import com.picturebook.book.enums.VisibilityType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyBookResponse {

    @Schema(description = "책 ID", example = "11111111-1111-1111-1111-111111111101")
    private UUID bookId;

    @Schema(description = "제목", example = "용감한 토끼의 모험")
    private String title;

    @Schema(description = "작가 이름", example = "장찬영")
    private String authorName;

    @Schema(description = "표지 이미지 URL", example = "https://cdn.example.com/cover/rabbit.png")
    private String coverImageUrl;

    @Schema(description = "책 상태", example = "COMPLETED")
    private BookStatus status;

    @Schema(description = "공개 범위", example = "PUBLIC")
    private VisibilityType visibility;

    @Schema(description = "생성 시각", example = "2026-06-01T12:00:00")
    private LocalDateTime createdAt;
}
