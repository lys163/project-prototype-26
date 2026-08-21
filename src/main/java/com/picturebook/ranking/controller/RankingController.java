package com.picturebook.ranking.controller;

import java.util.List;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.picturebook.global.response.ApiResponse;
import com.picturebook.ranking.dto.PopularAuthorResponse;
import com.picturebook.ranking.dto.PopularBookResponse;
import com.picturebook.ranking.dto.ProlificAuthorResponse;
import com.picturebook.ranking.service.RankingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Ranking", description = "랭킹 관련 API")
@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @Operation(
        summary = "월간 다작 작가 조회",
        description = "해당 월에 책을 가장 많이 발행한 작가 상위 3명을 조회합니다. "
                    + "year와 month를 생략하면 이번 달을 조회합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ProlificAuthorResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "유효하지 않은 날짜",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "RANK_001: 유효하지 않은 날짜 형식",
                    value = """
                    {
                        "status": 400,
                        "success": false,
                        "error": {
                            "code": "RANK_001",
                            "message": "유효하지 않은 날짜 형식입니다."
                        }
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/monthly/prolific-authors")
    public ResponseEntity<ApiResponse<?>> getProlificAuthors(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month
    ) {
        List<ProlificAuthorResponse> authors = rankingService.getMonthlyProlificAuthors(year, month);
        return ResponseEntity.ok(ApiResponse.ok(200, authors));
    }

    @Operation(
        summary = "월간 인기 작가 조회",
        description = "해당 월에 가장 많은 좋아요를 받은 작가 상위 3명을 조회합니다. "
                    + "year와 month를 생략하면 이번 달을 조회합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = PopularAuthorResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "유효하지 않은 날짜",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "RANK_001: 유효하지 않은 날짜 형식",
                    value = """
                    {
                        "status": 400,
                        "success": false,
                        "error": {
                            "code": "RANK_001",
                            "message": "유효하지 않은 날짜 형식입니다."
                        }
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/monthly/popular-authors")
    public ResponseEntity<ApiResponse<?>> getPopularAuthors(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month
    ) {
        List<PopularAuthorResponse> authors = rankingService.getMonthlyPopularAuthors(year, month);
        return ResponseEntity.ok(ApiResponse.ok(200, authors));
    }

    @Operation(
        summary = "월간 인기 책 조회",
        description = "해당 월에 가장 많은 좋아요를 받은 책 상위 3권을 조회합니다. "
                    + "year와 month를 생략하면 이번 달을 조회합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = PopularBookResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "유효하지 않은 날짜",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "RANK_001: 유효하지 않은 날짜 형식",
                    value = """
                    {
                        "status": 400,
                        "success": false,
                        "error": {
                            "code": "RANK_001",
                            "message": "유효하지 않은 날짜 형식입니다."
                        }
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/monthly/popular-books")
    public ResponseEntity<ApiResponse<?>> getPopularBooks(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month
    ) {
        List<PopularBookResponse> books = rankingService.getMonthlyPopularBooks(year, month);
        return ResponseEntity.ok(ApiResponse.ok(200, books));
    }

    @Operation(
        summary = "주간 다작 작가 조회",
        description = "해당 주에 책을 가장 많이 발행한 작가 상위 3명을 조회합니다. "
                    + "year·month·day를 생략하면 이번 주를 조회합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ProlificAuthorResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "유효하지 않은 날짜",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "RANK_001: 유효하지 않은 날짜 형식",
                    value = """
                    {
                        "status": 400,
                        "success": false,
                        "error": {
                            "code": "RANK_001",
                            "message": "유효하지 않은 날짜 형식입니다."
                        }
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/weekly/prolific-authors")
    public ResponseEntity<ApiResponse<?>> getProlificAuthors(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Integer day
    ) {
        List<ProlificAuthorResponse> authors = rankingService.getWeeklyProlificAuthors(year, month, day);

        return ResponseEntity.ok(ApiResponse.ok(200, authors));
    }

    @Operation(
        summary = "주간 인기 작가 조회",
        description = "해당 주에 가장 많은 좋아요를 받은 작가 상위 3명을 조회합니다. "
                    + "year·month·day를 생략하면 이번 주를 조회합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = PopularAuthorResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "유효하지 않은 날짜",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "RANK_001: 유효하지 않은 날짜 형식",
                    value = """
                    {
                        "status": 400,
                        "success": false,
                        "error": {
                            "code": "RANK_001",
                            "message": "유효하지 않은 날짜 형식입니다."
                        }
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/weekly/popular-authors")
    public ResponseEntity<ApiResponse<?>> getPopularAuthors(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Integer day
    ) {
        List<PopularAuthorResponse> authors = rankingService.getWeeklyPopularAuthors(year, month, day);
        return ResponseEntity.ok(ApiResponse.ok(200, authors));
    }

    @Operation(
        summary = "주간 인기 책 조회",
        description = "해당 주에 가장 많은 좋아요를 받은 책 상위 3권을 조회합니다. "
                    + "year·month·day를 생략하면 이번 주를 조회합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = PopularBookResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "유효하지 않은 날짜",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "RANK_001: 유효하지 않은 날짜 형식",
                    value = """
                    {
                        "status": 400,
                        "success": false,
                        "error": {
                            "code": "RANK_001",
                            "message": "유효하지 않은 날짜 형식입니다."
                        }
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/weekly/popular-books")
    public ResponseEntity<ApiResponse<?>> getPopularBooks(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Integer day
    ) {
        List<PopularBookResponse> books = rankingService.getWeeklyPopularBooks(year, month, day);
        return ResponseEntity.ok(ApiResponse.ok(200, books));
    }

}
