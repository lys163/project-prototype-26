package com.picturebook.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 카테고리 생성 요청 DTO (추후에 관리자 서버로 분리 예정)
public record CategoryCreateRequest(
    @NotBlank(message = "카테고리 이름은 필수입니다.")
    @Size(max = 50, message = "카테고리 이름은 최대 50자까지 허용됩니다.")
    String name,
    
    @NotNull(message = "카테고리 노출 순서는 필수입니다.")
    Integer displayOrder
) {
}