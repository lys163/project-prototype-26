package com.picturebook.follow.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.picturebook.follow.dto.AuthorFollowResponse;
import com.picturebook.follow.service.AuthorFollowService;
import com.picturebook.global.response.ApiResponse;
import com.picturebook.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "AuthorFollow", description = "작가 팔로우 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/authors/{authorId}/follow")
public class AuthorFollowController {

    private final AuthorFollowService authorFollowService;

    @Operation(summary = "작가 팔로우", description = "해당 작가를 팔로우하고 갱신된 팔로워 수와 팔로우 여부를 반환합니다. 인증 필수.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "팔로우 성공",
            content = @Content(schema = @Schema(implementation = AuthorFollowResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "FOLLOW_001: 자기 자신 팔로우",
                    value = """
                    {
                        "status": 400,
                        "success": false,
                        "error": {
                            "code": "FOLLOW_001",
                            "message": "자기 자신은 팔로우할 수 없습니다."
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
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
    @PostMapping
    public ResponseEntity<ApiResponse<AuthorFollowResponse>> followAuthor(
            @PathVariable UUID authorId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AuthorFollowResponse response = authorFollowService.followAuthor(
                authorId,
                userDetails.getUser().getId()
        );
        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }

    @Operation(
        summary = "작가 언팔로우",
        description = "해당 작가 팔로우를 해제합니다. 팔로우하고 있지 않아도 성공으로 처리됩니다(멱등). 인증 필수."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "언팔로우 성공",
            content = @Content(schema = @Schema(implementation = AuthorFollowResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "FOLLOW_001: 자기 자신 언팔로우",
                    value = """
                    {
                        "status": 400,
                        "success": false,
                        "error": {
                            "code": "FOLLOW_001",
                            "message": "자기 자신은 팔로우할 수 없습니다."
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
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
    @DeleteMapping
    public ResponseEntity<ApiResponse<AuthorFollowResponse>> unfollowAuthor(
            @PathVariable UUID authorId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AuthorFollowResponse response = authorFollowService.unfollowAuthor(
                authorId,
                userDetails.getUser().getId()
        );
        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }

    @Operation(
        summary = "작가 팔로우 상태 조회",
        description = "작가의 팔로워 수와 내 팔로우 여부를 조회합니다. 비로그인도 호출 가능하며, 이 경우 followedByMe는 항상 false입니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = AuthorFollowResponse.class))
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
    @GetMapping
    public ResponseEntity<ApiResponse<AuthorFollowResponse>> getAuthorFollow(
            @PathVariable UUID authorId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID followerId = userDetails != null ? userDetails.getUser().getId() : null;
        AuthorFollowResponse response = authorFollowService.getAuthorFollow(authorId, followerId);
        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }
}
