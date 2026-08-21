package com.picturebook.report.enums;

/**
 * 신고 처리 상태
 */
public enum ReportStatus {
    /** 접수됨 (검토 대기) */
    PENDING,
    /** 처리 완료 (조치됨) */
    RESOLVED,
    /** 기각 (문제 없음) */
    DISMISSED
}
