package com.picturebook.book.dto;

public record MonthlySalesResponse(
    // 월 (1~12)
    Integer month,
    // 해당 월 판매 건수
    Long salesCount
) {
}
