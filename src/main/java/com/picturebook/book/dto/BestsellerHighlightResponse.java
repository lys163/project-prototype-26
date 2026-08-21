package com.picturebook.book.dto;

import java.util.List;

import com.picturebook.book.enums.BestsellerPeriod;

public record BestsellerHighlightResponse(
    // 조회 기준 기간 (WEEKLY: 주간, MONTHLY: 월간)
    BestsellerPeriod period,
    // 해당 기간 판매 건수 상위 3권
    List<BestsellerItemResponse> items
) {
}
