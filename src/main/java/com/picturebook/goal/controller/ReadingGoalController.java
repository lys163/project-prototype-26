package com.picturebook.goal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.picturebook.global.response.ApiResponse;
import com.picturebook.global.security.CustomUserDetails;
import com.picturebook.goal.dto.ReadingGoalResponse;
import com.picturebook.goal.dto.SaveReadingGoalRequest;
import com.picturebook.goal.service.UserReadingGoalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@Tag(name = "ReadingGoal", description = "읽기 목표 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reading-goals")
public class ReadingGoalController {
    
    private final UserReadingGoalService userReadingGoalService;


    @Operation(
        summary = "이번 달 읽기 목표 저장",
        description = "이번 달 읽기 목표를 저장합니다. 이미 목표가 있으면 새 값으로 덮어씁니다. 인증 필수."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "저장 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 입력 값",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "INVALID_INPUT: 목표 값 유효성 검증 실패",
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
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PutMapping
    public ResponseEntity<ApiResponse<String>> saveReadingGoal(
        @Valid @RequestBody SaveReadingGoalRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userReadingGoalService.saveMonthlyReadingGoal(userDetails.getUser().getId(), request);
        return ResponseEntity.ok(ApiResponse.ok(200, "읽기 목표가 저장되었습니다."));
    }

    @Operation(
        summary = "월별 읽기 목표 조회",
        description = "해당 연월의 목표와 완독 수를 조회합니다. year와 month는 함께 보내거나 함께 생략해야 하며, "
                    + "생략하면 이번 달을 조회합니다. 목표를 설정한 적이 없으면 targetCount는 null로 반환됩니다. 인증 필수."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ReadingGoalResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 날짜 파라미터",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "GOAL_003: year와 month 중 하나만 전달됨",
                    value = """
                    {
                        "status": 400,
                        "success": false,
                        "error": {
                            "code": "GOAL_003",
                            "message": "유효하지 않은 목표 날짜입니다."
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<ReadingGoalResponse>> getMonthlyReadingGoal(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ReadingGoalResponse response = userReadingGoalService.getMonthlyReadingGoal(userDetails.getUser().getId(), year, month);

        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }
    
}
