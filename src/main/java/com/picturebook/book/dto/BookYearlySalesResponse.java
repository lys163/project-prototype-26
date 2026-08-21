package com.picturebook.book.dto;

import java.util.List;
import java.util.UUID;

public record BookYearlySalesResponse(
    UUID bookId,
    Integer year,
    // 1~12월 판매 건수 (판매가 없는 달도 0으로 채워 항상 12개)
    List<MonthlySalesResponse> monthlySales
) {
}
