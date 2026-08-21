package com.picturebook.book.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.picturebook.book.dto.BookDetailResponse;
import com.picturebook.book.dto.BookListResponse;
import com.picturebook.book.dto.BookPerformanceResponse;
import com.picturebook.book.dto.BookYearlySalesResponse;
import com.picturebook.book.dto.BestsellerBookResponse;
import com.picturebook.book.dto.BestsellerHighlightResponse;
import com.picturebook.book.dto.MyBookResponse;
import com.picturebook.book.dto.PaidPublishRequest;
import com.picturebook.book.enums.BestsellerPeriod;
import com.picturebook.book.enums.BookStatus;
import com.picturebook.book.service.BookService;
import com.picturebook.global.response.ApiResponse;
import com.picturebook.global.response.PageResponse;
import com.picturebook.global.security.CustomUserDetails;
import com.picturebook.like.dto.LikedBookResponse;
import com.picturebook.reading.dto.ReadingPageRequest;
import com.picturebook.reading.dto.ReadingProgressResponse;
import com.picturebook.reading.service.ReadingProgressService;
import com.picturebook.user.dto.UserCountResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Book", description = "책 목록·상세·베스트셀러·출판·성과 API")
public class BookController {

    private final BookService bookService;
    private final ReadingProgressService readingProgressService;

    @Operation(
            summary = "책 목록 조회",
            description = "공개(PUBLIC)·유료(PAID) 책을 출판일 최신순으로 조회합니다. 로그인 시 좋아요 여부(liked)가 채워집니다."
    )
    @ApiResponses(
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookListResponse>>> getBookList(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "카테고리 ID (미지정 시 전체)", example = "1")
            @RequestParam(required = false) Integer categoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = (userDetails != null) ? userDetails.getUser().getId() : null;
        PageResponse<BookListResponse> result = bookService.getPublicBookList(page, size, userId, categoryId);

        return ResponseEntity.ok(
                ApiResponse.ok(200, result)
        );
    }

    @Operation(
            summary = "내 책 목록 조회",
            description = "로그인 사용자가 만든 책을 생성일 최신순으로 조회합니다. status 미지정 시 DRAFT·IN_PROGRESS·COMPLETED 전체를 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PageResponse<MyBookResponse>>> getMyBookList(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "책 상태 필터 (DRAFT, IN_PROGRESS, COMPLETED)", example = "COMPLETED")
            @RequestParam(required = false) BookStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PageResponse<MyBookResponse> result = bookService.getMyBookList(
                page,
                size,
                userDetails.getUser().getId(),
                status
        );

        return ResponseEntity.ok(
                ApiResponse.ok(200, result)
        );
    }

    @Operation(
            summary = "내가 좋아요한 책 목록 조회",
            description = "로그인 사용자가 좋아요한 공개 책 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/me/likes")
    public ResponseEntity<ApiResponse<PageResponse<LikedBookResponse>>> getMyLikedBookList(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PageResponse<LikedBookResponse> result = bookService.getMyLikedBookList(
                page,
                size,
                userDetails.getUser().getId()
        );

        return ResponseEntity.ok(
                ApiResponse.ok(200, result)
        );
    }

    @Operation(
            summary = "베스트셀러 목록 조회",
            description = "완성된 유료(PAID) 책을 판매 건수 순으로 조회합니다. 응답에 순위(rank)가 포함됩니다."
    )
    @ApiResponses(
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    )
    @GetMapping("/bestsellers")
    public ResponseEntity<ApiResponse<PageResponse<BestsellerBookResponse>>> getBestsellers(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "카테고리 ID (미지정 시 전체)", example = "1")
            @RequestParam(required = false) Integer categoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = (userDetails != null) ? userDetails.getUser().getId() : null;
        PageResponse<BestsellerBookResponse> result = bookService.getBestsellers(page, size, userId, categoryId);

        return ResponseEntity.ok(
                ApiResponse.ok(200, result)
        );
    }

    @Operation(
            summary = "베스트셀러 하이라이트 조회",
            description = "주간(WEEKLY)/월간(MONTHLY) 판매 건수 기준 Top 3 책을 조회합니다."
    )
    @ApiResponses(
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    )
    @GetMapping("/bestsellers/highlights")
    public ResponseEntity<ApiResponse<BestsellerHighlightResponse>> getBestsellerHighlights(
            @Parameter(description = "집계 기간 (WEEKLY, MONTHLY)", example = "MONTHLY")
            @RequestParam(defaultValue = "MONTHLY") BestsellerPeriod period
    ) {
        BestsellerHighlightResponse result = bookService.getBestsellerHighlights(period);

        return ResponseEntity.ok(
                ApiResponse.ok(200, result)
        );
    }

    @Operation(
            summary = "책 상세 조회",
            description = "공개(PUBLIC)·유료(PAID) 책의 상세 정보(페이지·등장인물 포함)를 조회하며, 조회 시 조회수가 1 증가합니다. "
                    + "유료 책은 본인(작가) 또는 구매자만 전체 열람할 수 있고, 그 외에는 앞 3페이지만 미리보기로 제공됩니다(locked=true)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "책을 찾을 수 없음")
    })
    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<BookDetailResponse>> getBookDetail(
            @Parameter(description = "책 ID", example = "11111111-1111-1111-1111-111111111101")
            @PathVariable UUID bookId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = (userDetails != null) ? userDetails.getUser().getId() : null;
        BookDetailResponse result = bookService.getBookDetail(bookId, userId);

        return ResponseEntity.ok(
                ApiResponse.ok(200, result)
        );
    }

    @Operation(
            summary = "유료 출판",
            description = "본인 소유의 완성(COMPLETED) 책을 유료(PAID)로 전환하고 가격·공유 토큰·출판 시각을 설정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "유료 출판 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "완성되지 않은 책이거나 가격이 1 미만"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 도서가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책을 찾을 수 없음")
    })
    @PostMapping("/{bookId}/publish/paid")
    public ResponseEntity<ApiResponse<?>> publishAsPaid(
            @Parameter(description = "책 ID", example = "11111111-1111-1111-1111-111111111104")
            @PathVariable UUID bookId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PaidPublishRequest request
    ) {
        bookService.publishAsPaid(userDetails.getUser().getId(), bookId, request.price());

        return ResponseEntity.ok(ApiResponse.ok(200, "유료 출판 완료"));
    }

    @Operation(summary = "내가 만든 책 수 조회", description = "로그인 사용자가 만든 전체 책 개수를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/me/created/count")
    public ResponseEntity<ApiResponse<UserCountResponse>> getMyCreatedBookCount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserCountResponse result = bookService.getMyCreatedBookCount(userDetails.getUser().getId());

        return ResponseEntity.ok(
                ApiResponse.ok(200, result)
        );
    }

    @Operation(summary = "내가 공유한 책 수 조회", description = "공개(PUBLIC)·링크공유(LINK_SHARED) 상태인 내 책 개수를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/me/shared/count")
    public ResponseEntity<ApiResponse<UserCountResponse>> getMySharedBookCount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserCountResponse result = bookService.getMySharedBookCount(userDetails.getUser().getId());

        return ResponseEntity.ok(
                ApiResponse.ok(200, result)
        );
    }

    @Operation(summary = "내가 받은 좋아요 수 조회", description = "공개·링크공유 상태인 내 책들이 받은 좋아요 총합을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/me/received-likes/count")
    public ResponseEntity<ApiResponse<UserCountResponse>> getMyReceivedLikesCount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserCountResponse result = bookService.getMyReceivedLikesCount(userDetails.getUser().getId());

        return ResponseEntity.ok(
                ApiResponse.ok(200, result)
        );
    }

    @Operation(summary = "읽기 진행도 업데이트", description = "마지막으로 읽은 페이지 번호를 갱신합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "업데이트 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PutMapping("/{bookId}/reading-progress")
    public ResponseEntity<ApiResponse<?>> saveReadingProgress(
        @Parameter(description = "책 ID", example = "11111111-1111-1111-1111-111111111101")
        @PathVariable UUID bookId,
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody @Valid ReadingPageRequest request
    ) {
        readingProgressService.updateProgress(userDetails.getUser().getId(), bookId, request.lastReadPageNumber());

        return ResponseEntity.ok(ApiResponse.ok(200, "책 진행도 업데이트 완료"));
    }

    @Operation(summary = "완독 처리", description = "마지막으로 읽은 페이지 번호 갱신과 함께 완독 시각을 기록합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "완독 처리 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping("/{bookId}/reading-progress/complete")
    public ResponseEntity<ApiResponse<?>> completeReadingProgress(
        @Parameter(description = "책 ID", example = "11111111-1111-1111-1111-111111111101")
        @PathVariable UUID bookId,
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody @Valid ReadingPageRequest request
    ) {
        readingProgressService.completeProgress(userDetails.getUser().getId(), bookId, request.lastReadPageNumber());

        return ResponseEntity.ok(ApiResponse.ok(200, "완독 처리 완료"));
    }

    @Operation(
            summary = "내 진행도 조회",
            description = "특정 책에 대한 마지막 읽은 페이지(lastReadPageNumber)와 완독 여부(isCompleted)를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/{bookId}/reading-progress")
    public ResponseEntity<ApiResponse<ReadingProgressResponse>> getReadingProgress(
        @Parameter(description = "책 ID", example = "11111111-1111-1111-1111-111111111101")
        @PathVariable UUID bookId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ReadingProgressResponse result = readingProgressService.getProgress(userDetails.getUser().getId(), bookId);

        return ResponseEntity.ok(ApiResponse.ok(200, result));
    }

    @Operation(
            summary = "작품별 성과 조회",
            description = "내 공개·유료 완성작의 조회수·좋아요 수·평점·수익을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/me/performance")
    public ResponseEntity<ApiResponse<PageResponse<BookPerformanceResponse>>> getBookPerformance(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PageableDefault(page = 0, size = 10) Pageable pageable
        ) {
        PageResponse<BookPerformanceResponse> response = bookService.getBookPerformance(userDetails.getUser().getId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }

    @Operation(
            summary = "작품별 월 판매 건수 추이 조회",
            description = "특정 작품의 연도별 1~12월 판매 건수를 조회합니다. year 미지정 시 올해 기준입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/{bookId}/sales/monthly")
    public ResponseEntity<ApiResponse<BookYearlySalesResponse>> getBookMonthlySales(
        @Parameter(description = "책 ID", example = "11111111-1111-1111-1111-111111111104")
        @PathVariable UUID bookId,
        @Parameter(description = "조회 연도 (미지정 시 올해)", example = "2026")
        @RequestParam(required = false) Integer year,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        BookYearlySalesResponse response = bookService.getBookMonthlySales(
                userDetails.getUser().getId(), bookId, year);
        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }

}
