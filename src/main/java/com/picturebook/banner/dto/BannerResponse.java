package com.picturebook.banner.dto;

import java.util.UUID;

import com.picturebook.banner.entity.Banner;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "배너 이미지 응답")
public record BannerResponse(
        @Schema(description = "배너 ID")
        UUID bannerId,

        @Schema(description = "배너 제목")
        String title,

        @Schema(description = "배너 이미지 URL")
        String imageUrl,

        @Schema(description = "배너 클릭 시 이동 URL")
        String linkUrl,

        @Schema(description = "배너 노출 순서")
        Integer displayOrder
) {
    public static BannerResponse from(Banner banner) {
        return new BannerResponse(
                banner.getId(),
                banner.getTitle(),
                banner.getImageUrl(),
                banner.getLinkUrl(),
                banner.getDisplayOrder()
        );
    }
}
