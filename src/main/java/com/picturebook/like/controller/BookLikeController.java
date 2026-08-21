package com.picturebook.like.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.picturebook.global.response.ApiResponse;
import com.picturebook.global.security.CustomUserDetails;
import com.picturebook.like.dto.BookLikeResponse;
import com.picturebook.like.service.BookLikeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books/{bookId}/likes")
@Tag(name = "Book Like", description = "책 좋아요 API")
public class BookLikeController {

    private final BookLikeService bookLikeService;

    @PostMapping
    @Operation(
        summary = "책 좋아요 등록",
        description = "해당 책에 좋아요를 등록하고 갱신된 좋아요 수와 내 좋아요 여부를 반환합니다. "
                    + "이미 좋아요한 상태에서 호출해도 성공으로 처리됩니다(멱등). 인증 필수."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "좋아요 등록 성공",
            content = @Content(schema = @Schema(implementation = BookLikeResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "도서 없음",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "BOOK_001: 도서를 찾을 수 없음",
                    value = """
                    {
                        "status": 404,
                        "success": false,
                        "error": {
                            "code": "BOOK_001",
                            "message": "해당 도서를 찾을 수 없습니다."
                        }
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<BookLikeResponse>> likeBook(
            @PathVariable UUID bookId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        BookLikeResponse response = bookLikeService.likeBook(bookId, userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }

    @DeleteMapping
    @Operation(
        summary = "책 좋아요 취소",
        description = "해당 책의 좋아요를 취소합니다. 좋아요하지 않은 상태에서 호출해도 성공으로 처리됩니다(멱등). 인증 필수."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "좋아요 취소 성공",
            content = @Content(schema = @Schema(implementation = BookLikeResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "도서 없음",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "BOOK_001: 도서를 찾을 수 없음",
                    value = """
                    {
                        "status": 404,
                        "success": false,
                        "error": {
                            "code": "BOOK_001",
                            "message": "해당 도서를 찾을 수 없습니다."
                        }
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<BookLikeResponse>> unlikeBook(
            @PathVariable UUID bookId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        BookLikeResponse response = bookLikeService.unlikeBook(bookId, userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }

    @GetMapping
    @Operation(
        summary = "책 좋아요 조회",
        description = "해당 책의 좋아요 수와 내 좋아요 여부를 조회합니다. "
                    + "비로그인도 호출 가능하며, 이 경우 likedByMe는 항상 false입니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = BookLikeResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "도서 없음",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "BOOK_001: 도서를 찾을 수 없음",
                    value = """
                    {
                        "status": 404,
                        "success": false,
                        "error": {
                            "code": "BOOK_001",
                            "message": "해당 도서를 찾을 수 없습니다."
                        }
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<BookLikeResponse>> getBookLike(
            @PathVariable UUID bookId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails != null ? userDetails.getUser().getId() : null;
        BookLikeResponse response = bookLikeService.getBookLike(bookId, userId);
        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }
}
