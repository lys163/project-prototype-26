package com.picturebook.report.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.picturebook.global.response.ApiResponse;
import com.picturebook.global.security.CustomUserDetails;
import com.picturebook.report.dto.ReportRequest;
import com.picturebook.report.dto.ReportResponse;
import com.picturebook.report.enums.ReportStatus;
import com.picturebook.report.service.ReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;




@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Tag(name = "Report", description = "도서 신고 관리 API")
public class ReportController {

    private final ReportService reportService;

    // 신고 등록
    @Operation(summary = "도서 신고 등록", description = "특정 도서를 신고합니다. 본인 도서는 신고할 수 없으며 중복 신고도 불가능합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "신고 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = {
                    @ExampleObject(
                        name = "REPORT_002: 본인 도서 신고",
                        value = """
                        {
                            "status": 400,
                            "success": false,
                            "error": {
                                "code": "REPORT_002",
                                "message": "본인의 도서는 신고할 수 없습니다."
                            }
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "INVALID_INPUT: 신고 사유 유효성 검증 실패",
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
            description = "도서를 찾을 수 없음",
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
            description = "중복 신고",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "REPORT_001: 이미 신고한 도서",
                    value = """
                    {
                        "status": 409,
                        "success": false,
                        "error": {
                            "code": "REPORT_001",
                            "message": "이미 해당 도서에 대한 신고 기록이 존재합니다."
                        }
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/{bookId}")
    public ResponseEntity<ApiResponse<?>> report(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable UUID bookId,
        @Valid @RequestBody ReportRequest request
    ) {
        reportService.createReport(bookId, userDetails.getUser().getId(), request);

        return ResponseEntity.ok(ApiResponse.ok(200,"신고가 등록되었습니다."));
    }
    @Operation(
        summary = "내 신고 목록 조회",
        description = "현재 로그인한 사용자가 작성한 신고 내역을 최신순으로 조회합니다. 상태별 필터링이 가능합니다.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
        }
    )
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyReports(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Parameter(description = "신고 상태 (PENDING, RESOLVED, DISMISSED)", example = "PENDING")
        @RequestParam(required = false) ReportStatus status,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ReportResponse> reportResponses = reportService.getReportsByUser(userDetails.getUser().getId(), status, pageable);

        return ResponseEntity.ok(ApiResponse.ok(200, reportResponses));
    }
    
}
