package com.picturebook.report.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.picturebook.report.entity.Report;
import com.picturebook.report.enums.ReportReason;
import com.picturebook.report.enums.ReportStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "신고 등록 응답")
public record ReportResponse(
    @Schema(description = "신고 ID")
    UUID id,

    @Schema(description = "신고된 책 ID")
    UUID bookId,

    @Schema(description = "신고된 책 이름")
    String bookTitle,

    @Schema(description = "신고자 ID")
    UUID reporterId,

    @Schema(description = "신고 사유")
    ReportReason reason,

    @Schema(description = "기타 사유 상세 (reason이 OTHER일 때 사용)")
    String detail,

    @Schema(description = "처리 상태")
    ReportStatus status,

    @Schema(description = "생성 일자")
    LocalDateTime createdAt
) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(
            report.getId(),
            report.getBookId(),
            report.getBookTitle(),
            report.getReporterId(),
            report.getReason(),
            report.getDetail(),
            report.getStatus(),
            report.getCreatedAt()
        );
    }
}
