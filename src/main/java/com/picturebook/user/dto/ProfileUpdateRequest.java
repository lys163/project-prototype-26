package com.picturebook.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProfileUpdateRequest(
    @Schema(description = "변경할 닉네임", example = "aaa")
    String nickname,
    @Schema(description = "변경할 이메일(중복 불가)", example = "new@example.com")
    String email,
    @Schema(description = "MinIO IMAGE URL")
    String profileImage
) {
    
}
