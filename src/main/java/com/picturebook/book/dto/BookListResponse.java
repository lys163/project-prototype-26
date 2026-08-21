package com.picturebook.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class BookListResponse {

    @Schema(description = "책 ID", example = "11111111-1111-1111-1111-111111111101")
    private UUID bookId;

    @Schema(description = "제목", example = "용감한 토끼의 모험")
    private String title;

    @Schema(description = "표지 이미지 URL", example = "https://cdn.example.com/cover/rabbit.png")
    private String coverImageUrl;

    @Schema(description = "작가 이름", example = "장찬영")
    private String authorName;

    @Schema(description = "가격(원). null 또는 0이면 무료, 양수면 유료", example = "3000")
    private Integer price;

    @Schema(description = "로그인 사용자의 좋아요 여부", example = "false")
    private boolean liked;
}
