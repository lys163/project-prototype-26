package com.picturebook.auth.controller;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.picturebook.auth.dto.TokenResponseDto;
import com.picturebook.auth.service.AuthService;
import com.picturebook.global.response.ApiResponse;
import com.picturebook.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;


@Tag(name = "Auth", description = "인증 · 토큰 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @Operation(
        summary = "로그아웃",
        description = "Redis에 저장된 Refresh Token을 삭제하고 refreshToken 쿠키를 만료시킵니다. "
                    + "이미 로그아웃된 상태에서 호출해도 성공으로 처리됩니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "로그아웃 성공 (응답 본문 없음)"
        )
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        HttpServletResponse response
    ) {
        if (userDetails != null){
            authService.logout(userDetails.getUser().getId());
        }

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
            .path("/")
            .httpOnly(true)
            .secure(true) // https에서는 ture로 변경
            .maxAge(0)
            .sameSite("Lax")
            .build();

        response.addHeader("Set-Cookie", cookie.toString());


        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "토큰 재발급", description = "쿠키의 Refresh Token을 확인하여 새로운 Access Token을 발급합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "재발급 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "인증 실패 (다양한 케이스 확인)",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = {
                    @ExampleObject(
                        name = "AUTH_001: 유효하지 않은 토큰",
                        value = """
                        {
                            "status": 401,
                            "success": false,
                            "error": {
                                "code": "AUTH_001",
                                "message": "유효하지 않은 리프레시 토큰입니다." 
                            }
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "AUTH_002: 토큰 만료/부재",
                        value = """
                            {
                            "status": 401,
                            "success": false,
                            "error": {
                                "code": "AUTH_002",
                                "message": "리프레시 토큰이 만료되었거나 존재하지 않습니다." 
                                }
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "AUTH_003: 토큰 불일치",
                        value = """
                            {
                            "status": 401,
                            "success": false,
                            "error": {
                                "code": "AUTH_003", 
                                "message": "리프레시 토큰이 일치하지 않습니다." 
                            }
                            }
                            """
                    )
                }
            )
        )
})
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponseDto>> refresh(
        @CookieValue(name = "refreshToken") String refreshToken,
        HttpServletResponse response
    ){
        return ResponseEntity.ok(ApiResponse.ok(200, TokenResponseDto
            .from(authService.refreshAccessToken(refreshToken))));
    }

}
