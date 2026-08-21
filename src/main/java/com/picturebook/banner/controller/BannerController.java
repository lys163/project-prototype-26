package com.picturebook.banner.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.picturebook.banner.dto.BannerResponse;
import com.picturebook.banner.service.BannerService;
import com.picturebook.global.response.ApiResponse;
import com.picturebook.global.response.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@Tag(name = "Banner", description = "배너 이미지 API")
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/banners")
public class BannerController {

    private final BannerService bannerService;

    @Operation(
        summary = "배너 이미지 목록 조회",
        description = "활성화된 배너 이미지를 노출 순서대로 조회합니다. 인증 없이 호출 가능합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = BannerResponse.class))
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
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BannerResponse>>> getBanners(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        PageResponse<BannerResponse> response = bannerService.getActiveBanners(page, size);
        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }
}
