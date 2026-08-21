package com.picturebook.review.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.picturebook.global.response.ApiResponse;
import com.picturebook.global.response.PageResponse;
import com.picturebook.global.security.CustomUserDetails;
import com.picturebook.review.dto.ReviewDeleteResponse;
import com.picturebook.review.dto.ReviewRequest;
import com.picturebook.review.dto.ReviewResponse;
import com.picturebook.review.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@Tag(name = "Review", description = "리뷰 API")
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(
        summary = "책 리뷰 목록 조회",
        description = "공개 책의 리뷰 목록을 최신순으로 조회합니다. "
                    + "비로그인도 호출 가능하며, 이 경우 각 리뷰의 '내가 쓴 리뷰' 여부는 항상 false입니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ReviewResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 페이지 파라미터 (page < 0 또는 size 범위 초과)",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "INVALID_INPUT: 페이지 파라미터 유효성 검증 실패",
                    value = """
                    {
                        "status": 400,
                        "success": false,
                        "error": {
                            "code": "INVALID_INPUT",
                            "message": "잘못된 입력 값입니다."
                        }
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/books/{bookId}/reviews")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getBookReviews(
            @PathVariable UUID bookId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        UUID currentUserId = userDetails != null ? userDetails.getUser().getId() : null;
        PageResponse<ReviewResponse> response = reviewService.getBookReviews(bookId, currentUserId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }

    @Operation(summary = "내 리뷰 목록 조회", description = "현재 로그인한 사용자가 작성한 리뷰 목록을 페이지 형태로 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ReviewResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "사용자 없음",
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
    @GetMapping("/reviews/me")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getMyReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        PageResponse<ReviewResponse> response = reviewService.getMyReviews(userDetails.getUser().getId(), page, size);
        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }

    @Operation(summary = "리뷰 생성", description = "공개 책에 리뷰를 작성합니다. 책당 사용자 1명은 리뷰 1개만 작성할 수 있습니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "리뷰 작성 성공",
            content = @Content(schema = @Schema(implementation = ReviewResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = {
                    @ExampleObject(
                        name = "REVIEW_002: 본인 도서에 리뷰 작성",
                        value = """
                        {
                            "status": 400,
                            "success": false,
                            "error": {
                                "code": "REVIEW_002",
                                "message": "본인의 도서에는 리뷰를 작성할 수 없습니다."
                            }
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "INVALID_INPUT: 평점/내용 유효성 검증 실패",
                        value = """
                        {
                            "status": 400,
                            "success": false,
                            "error": {
                                "code": "INVALID_INPUT",
                                "message": "잘못된 입력 값입니다."
                            }
                        }
                        """
                    )
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "공개 도서 없음",
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
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "이미 리뷰를 작성함",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "REVIEW_001: 중복 리뷰",
                    value = """
                    {
                        "status": 409,
                        "success": false,
                        "error": {
                            "code": "REVIEW_001",
                            "message": "이미 해당 도서에 리뷰를 작성하였습니다."
                        }
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/books/{bookId}/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable UUID bookId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewRequest request
    ) {
        ReviewResponse response = reviewService.createReview(bookId, userDetails.getUser().getId(), request);
        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }

    @Operation(
        summary = "리뷰 수정",
        description = "내가 작성한 리뷰를 수정합니다. 다른 사람의 리뷰를 수정하려 하면 조회되지 않아 404가 반환됩니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "리뷰 수정 성공",
            content = @Content(schema = @Schema(implementation = ReviewResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 입력 값",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "INVALID_INPUT: 평점/내용 유효성 검증 실패",
                    value = """
                    {
                        "status": 400,
                        "success": false,
                        "error": {
                            "code": "INVALID_INPUT",
                            "message": "잘못된 입력 값입니다."
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "리뷰 또는 도서 없음",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = {
                    @ExampleObject(
                        name = "REVIEW_003: 내 리뷰를 찾을 수 없음",
                        value = """
                        {
                            "status": 404,
                            "success": false,
                            "error": {
                                "code": "REVIEW_003",
                                "message": "해당 리뷰를 찾을 수 없습니다."
                            }
                        }
                        """
                    ),
                    @ExampleObject(
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
                }
            )
        )
    })
    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewRequest request
    ) {
        ReviewResponse response = reviewService.updateReview(reviewId, userDetails.getUser().getId(), request);
        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }

    @Operation(
        summary = "리뷰 삭제",
        description = "내가 작성한 리뷰를 삭제합니다. 다른 사람의 리뷰를 삭제하려 하면 조회되지 않아 404가 반환됩니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "리뷰 삭제 성공",
            content = @Content(schema = @Schema(implementation = ReviewDeleteResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "리뷰 없음",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "REVIEW_003: 내 리뷰를 찾을 수 없음",
                    value = """
                    {
                        "status": 404,
                        "success": false,
                        "error": {
                            "code": "REVIEW_003",
                            "message": "해당 리뷰를 찾을 수 없습니다."
                        }
                    }
                    """
                )
            )
        )
    })
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDeleteResponse>> deleteReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ReviewDeleteResponse response = reviewService.deleteReview(reviewId, userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }
}
