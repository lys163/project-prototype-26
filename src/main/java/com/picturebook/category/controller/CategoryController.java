package com.picturebook.category.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.picturebook.category.dto.CategoryCreateRequest;
import com.picturebook.category.dto.CategoryListResponse;
import com.picturebook.category.service.CategoryService;
import com.picturebook.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "카테고리 API")
public class CategoryController {
    
    private final CategoryService categoryService;

    // 카테고리 생성 (추후에 관리자 서버로 분리 예정)
    @Operation(summary = "카테고리 생성", description = "새 카테고리를 생성합니다. 동일한 이름이 이미 있으면 409를 반환합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 입력 값",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "INVALID_INPUT: 유효성 검증 실패",
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
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "카테고리 중복",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "CATEGORY_001: 이미 존재하는 카테고리",
                    value = """
                    {
                        "status": 409,
                        "success": false,
                        "error": {
                            "code": "CATEGORY_001",
                            "message": "이미 존재하는 카테고리입니다."
                        }
                    }
                    """
                )
            )
        )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<String>> createCategory(@RequestBody @Valid CategoryCreateRequest request) {
        categoryService.createCategory(request);
        return ResponseEntity.status(201).body(ApiResponse.ok(201, "카테고리가 성공적으로 생성되었습니다."));
    }

    @Operation(summary = "카테고리 목록 조회", description = "등록된 전체 카테고리를 조회합니다. 인증 없이 호출 가능합니다.")
    @ApiResponses(
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = CategoryListResponse.class))
        )
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryListResponse>>> getCategories(){
        return ResponseEntity.ok(ApiResponse.ok(200, categoryService.getCategories()));
    }
}
