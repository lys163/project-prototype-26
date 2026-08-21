package com.picturebook.user.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.picturebook.book.dto.AuthorBookResponse;
import com.picturebook.global.response.ApiResponse;
import com.picturebook.global.response.PageResponse;
import com.picturebook.user.dto.AuthorStatsResponse;
import com.picturebook.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
@Tag(name = "Author", description = "작가 공개 정보 API")
public class AuthorController {

    private final UserService userService;

    @Operation(
        summary = "작가 통계 조회",
        description = "작가의 작품 수, 총 좋아요, 평균 평점, 팔로워 수를 조회합니다. 인증 없이 호출 가능합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = AuthorStatsResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "작가 없음",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "USER_001: 사용자를 찾을 수 없음",
                    value = """
                    {
                        "status": 404,
                        "success": false,
                        "error": {
                            "code": "USER_001",
                            "message": "사용자를 찾을 수 없습니다."
                        }
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/{authorId}/stats")
    public ResponseEntity<ApiResponse<AuthorStatsResponse>> getAuthorStats(
            @PathVariable UUID authorId
    ) {
        AuthorStatsResponse response = userService.getAuthorStats(authorId);

        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }

    @Operation(
        summary = "작가 작품 목록 조회",
        description = "작가가 만든 책 중 완성된(COMPLETED) 공개(PUBLIC)/유료(PAID) 작품 목록을 출판일 최신순으로 조회합니다. "
                    + "출판일이 없는 작품은 항상 뒤로 정렬됩니다. 인증 없이 호출 가능합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = AuthorBookResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "작가 없음",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "USER_001: 사용자를 찾을 수 없음",
                    value = """
                    {
                        "status": 404,
                        "success": false,
                        "error": {
                            "code": "USER_001",
                            "message": "사용자를 찾을 수 없습니다."
                        }
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/{authorId}/books")
    public ResponseEntity<ApiResponse<PageResponse<AuthorBookResponse>>> getAuthorBooks(
            @PathVariable UUID authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<AuthorBookResponse> response = userService.getAuthorBooks(authorId, page, size);

        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }
}
