package com.picturebook.user.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.picturebook.global.response.ApiResponse;
import com.picturebook.global.response.PageResponse;
import com.picturebook.global.security.CustomUserDetails;
import com.picturebook.reading.dto.MyReadingProgressResponse;
import com.picturebook.reading.service.ReadingProgressService;
import com.picturebook.user.dto.AuthorProfileResponse;
import com.picturebook.user.dto.AuthorSummaryResponse;
import com.picturebook.user.dto.ProfileImageUpdateRequest;
import com.picturebook.user.dto.ProfileUpdateRequest;
import com.picturebook.user.dto.ReaderDashboardResponse;
import com.picturebook.user.dto.UserResponseDto;
import com.picturebook.user.dto.YearlyRevenueResponse;
import com.picturebook.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "유저 프로필 관리 API")
public class UserController {

    private final UserService userService;
    private final ReadingProgressService readingProgressService;

    @Operation(
        summary = "내 정보 조회",
        description = "로그인한 사용자의 프로필을 조회합니다. 소셜 로그인 최초 가입자인 경우 isNewUser가 true로 내려갑니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = UserResponseDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getMyinfo(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getUser().getId();
        boolean isNewUser = userDetails.isNewUser();

        UserResponseDto response = userService.getUserDto(userId, isNewUser);
        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }

    @Operation(summary = "프로필 이미지 수정", description = "IMAGE URL을 받아 프로필 사진 업데이트")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "이미지 수정 성공",
            content = @Content(schema = @Schema(implementation = UserResponseDto.class))
        ),
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
    @PatchMapping("/profile-image")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody ProfileImageUpdateRequest body
    ) {
        UserResponseDto responseDto = userService.updateProfileImage(
            userDetails.getUser().getId(),
            body.getProfileImage()
        );
        return ResponseEntity.ok(ApiResponse.ok(200, responseDto));
    }

    @Operation(summary = "프로필 정보 수정", description = "닉네임과 이메일을 수정합니다. 이메일 변경 시 중복 검사를 수행합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "정보 수정 성공",
            content = @Content(schema = @Schema(implementation = UserResponseDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "비즈니스 로직 에러 (이메일 중복)",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "USER_002: 이메일 중복",
                    value = """
                    {
                        "status": 409,
                        "success": false,
                        "error": {
                            "code": "USER_002",
                            "message": "이미 존재하는 이메일입니다."
                        }
                    }
                    """
                )
            )
        ),
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
    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateProfileInfo(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody ProfileUpdateRequest request
    ) {
        UserResponseDto responseDto = userService.updateProfile(userDetails.getUser().getId(), request);
        return ResponseEntity.ok(ApiResponse.ok(200, responseDto));
    }

    // 내가 읽고 있는 책 목록 조회 API (bookId, 완독 포함 여부, 제목, 작가, 마지막 읽은 날짜 계산, 진행도)
    @Operation(
        summary = "내가 읽고 있는 책 목록 조회",
        description = "읽기 진행 중인 책을 최근 읽은 순으로 조회합니다. includeCompleted=true로 보내면 완독한 책도 포함합니다. 인증 필수."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = MyReadingProgressResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/me/reading-progresses")
    public ResponseEntity<ApiResponse<PageResponse<MyReadingProgressResponse>>> getMyReadingProgresses(
        @RequestParam(defaultValue = "false") boolean includeCompleted,
        @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PageResponse<MyReadingProgressResponse> response = readingProgressService.getMyReadingProgresses(userDetails.getUser().getId(), includeCompleted, pageable);

        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }

    @Operation(
        summary = "독자 대시보드 조회",
        description = "최근 읽은 책과 읽기 통계를 한 번에 조회합니다. 인증 필수."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ReaderDashboardResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/me/reader-dashboard")
    public ResponseEntity<ApiResponse<ReaderDashboardResponse>> getReaderDashboard(
        @PageableDefault(size = 5, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ReaderDashboardResponse response = userService.getReaderDashboard(userDetails.getUser().getId(), pageable);

        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }
    
    // 작가 수익 조회 API (연도별 월 수익)
    @Operation(
        summary = "작가 수익 조회",
        description = "해당 연도의 1~12월 판매 수익을 조회합니다. year를 생략하면 올해를 조회합니다. 인증 필수."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = YearlyRevenueResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/me/revenue")
    public ResponseEntity<ApiResponse<YearlyRevenueResponse>> getAuthorRevenue(
        @RequestParam(required = false) Integer year,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        YearlyRevenueResponse response = userService.getYearlyRevenue(userDetails.getUser().getId(), year);

        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }
    
    @Operation(
        summary = "작가 요약 정보 조회",
        description = "출판한 작품 수, 총 수익 등 작가 대시보드 상단 요약 지표를 조회합니다. 인증 필수."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = AuthorSummaryResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/me/summary")
    public ResponseEntity<ApiResponse<AuthorSummaryResponse>> getAuthorSummary(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AuthorSummaryResponse response = userService.getAuthorSummary(userDetails.getUser().getId());

        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }

    // 작가 프로필 조회 API (이름, 가입일, 소개)
    @Operation(summary = "작가 프로필 조회", description = "작가의 이름, 가입일, 소개를 조회합니다. 인증 없이 호출 가능합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = AuthorProfileResponse.class))
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
    @GetMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<AuthorProfileResponse>> getAuthorProfile(
        @PathVariable UUID userId
    ) {
        AuthorProfileResponse response = userService.getAuthorProfile(userId);

        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }
}
