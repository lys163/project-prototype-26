package com.picturebook.storage.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.picturebook.storage.dto.PresignedUploadRequest;
import com.picturebook.storage.dto.PresignedUrlResponse;
import com.picturebook.storage.service.StorageUseCase;
import com.picturebook.global.security.CustomUserDetails;
import com.picturebook.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Storage", description = "오브젝트 스토리지 프리사인드 URL API")
@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageUseCase storageUseCase;

    @Operation(
            summary = "업로드용 프리사인드 POST form 발급",
            description = "클라이언트는 반환된 uploadUrl과 fields로 multipart/form-data POST를 전송한다. 인증 필수."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프리사인드 POST form 발급 성공",
                    content = @Content(schema = @Schema(implementation = PresignedUrlResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 또는 지원하지 않는 MIME/파일 크기")
    })
    @PostMapping("/presigned-upload")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> presignedUpload(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PresignedUploadRequest request
    ) {
        PresignedUrlResponse response = storageUseCase.issueUploadUrl(
                userDetails.getUser().getId(),
                request
        );
        return ResponseEntity.ok(ApiResponse.ok(200, response));
    }
}
