package com.picturebook.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // AUTH ERRORCODE
    INVALID_REFRESH_TOKEN(401, "AUTH_001", "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(401, "AUTH_002", "리프레시 토큰이 만료되었거나 존재하지 않습니다."),
    REFRESH_TOKEN_MISMATCH(401, "AUTH_003", "리프레시 토큰이 일치하지 않습니다."),

    // USER
    USER_NOT_FOUND(404, "USER_001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(409, "USER_002", "이미 존재하는 이메일입니다."),

    // Report
    ALREADY_REPORTED(409, "REPORT_001", "이미 해당 도서에 대한 신고 기록이 존재합니다."),
    CANNOT_REPORT_OWN_BOOK(400, "REPORT_002", "본인의 도서는 신고할 수 없습니다."),

    // BOOK
    BOOK_NOT_FOUND(404, "BOOK_001", "해당 도서를 찾을 수 없습니다."),
    PAGE_BAD_REQUEST(400, "BOOK_002", "요청 페이지가 유효 범위를 벗어났습니다."),
    BOOK_FORBIDDEN(403, "BOOK_003", "본인의 도서만 출판할 수 있습니다."),
    BOOK_NOT_COMPLETED(400, "BOOK_004", "완성된 도서만 유료 출판할 수 있습니다."),
    BOOK_SALES_FORBIDDEN(403, "BOOK_005", "해당 도서의 판매량을 조회할 권한이 없습니다."),

    // REVIEW
    ALREADY_REVIEWED(409, "REVIEW_001", "이미 해당 도서에 리뷰를 작성하였습니다."),
    CANNOT_REVIEW_OWN_BOOK(400, "REVIEW_002", "본인의 도서에는 리뷰를 작성할 수 없습니다."),
    REVIEW_NOT_FOUND(404, "REVIEW_003", "해당 리뷰를 찾을 수 없습니다."),

    // ranking
    INVALID_DATE_FORMAT(400, "RANK_001", "유효하지 않은 날짜 형식입니다."),

    // FOLLOW
    CANNOT_FOLLOW_SELF(400, "FOLLOW_001", "자기 자신은 팔로우할 수 없습니다."),

    // GOAL
    INVALID_GOAL_MONTH(400, "GOAL_001", "월(month)은 1~12 사이여야 합니다."),
    INVALID_GOAL_VALUE(400, "GOAL_002", "목표는 1~999 사이여야 합니다."),
    INVALID_GOAL_DATE(400, "GOAL_003", "유효하지 않은 목표 날짜입니다."),

    // INVALID
    INVALID_INPUT_VALUE(400, "INVALID_INPUT", "잘못된 입력 값입니다."),

    // Category
    CATEGORY_ALREADY_EXISTS(409, "CATEGORY_001", "이미 존재하는 카테고리입니다.");
    
    private final int status;
    private final String code;
    private final String message;

}
