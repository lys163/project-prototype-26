package com.picturebook.report.entity;

import com.picturebook.global.entity.BaseCreatedEntity;
import com.picturebook.report.enums.ReportReason;
import com.picturebook.report.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 신고
 * <p>
 * 유저가 부적절한 책을 신고한 기록입니다.
 * 관리자가 신고를 검토하고 처리(RESOLVED) 또는 기각(DISMISSED) 합니다.
 */
@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 신고자 → users.id */
    @Column(name = "reporter_id", nullable = false, columnDefinition = "uuid")
    private UUID reporterId;

    /** 신고 대상 책 → books.id */
    @Column(name = "book_id", nullable = false, columnDefinition = "uuid")
    private UUID bookId;

    /* 신고 대상 책 제목 -> books.title 
    - 추가 이유 -
    신고 시점의 책 제목을 저장하여,
    책이 삭제되거나 제목이 변경되어도 신고 기록이 유지되도록 함(ux)
    내 신고 목록을 볼 때 bookId로 책 제목을 조회하면 성능 이슈 발생
    */ 
    @Column(name = "book_title", nullable = false)
    private String bookTitle; 

    /** 신고 사유 */
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private ReportReason reason;

    /** 기타 사유 상세 (reason이 OTHER일 때 사용) */
    @Column(name = "detail", length = 500)
    private String detail;

    /** 처리 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status;

    @Builder
    private Report(UUID reporterId, UUID bookId, String bookTitle, ReportReason reason, String detail) {
        this.reporterId = reporterId;
        this.bookId = bookId;
        this.bookTitle = bookTitle; // Assuming bookTitle is passed as a parameter
        this.reason = reason;
        this.detail = detail;
        this.status = ReportStatus.PENDING;
    }

    /** 신고 처리 완료 */
    public void resolve() {
        this.status = ReportStatus.RESOLVED;
    }

    /** 신고 기각 */
    public void dismiss() {
        this.status = ReportStatus.DISMISSED;
    }
}
