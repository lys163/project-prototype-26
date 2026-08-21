package com.picturebook.report.dto;

import com.picturebook.report.enums.ReportReason;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "신고 등록 요청")
public record ReportRequest(

    @Schema(description = "신고 사유", example = "SPAM")
    @NotNull(message = "신고 사유는 필수입니다.")
    ReportReason reason,

    @Schema(description = "기타 사유 상세 (reason이 OTHER일 때 사용)", example = "이 책은 광고성 콘텐츠입니다.")
    @Size(max = 500, message = "상세 내용은 500자 이내여야 합니다.")
    String detail
) {
    
}
