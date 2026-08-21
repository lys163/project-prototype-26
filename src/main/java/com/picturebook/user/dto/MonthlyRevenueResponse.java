package com.picturebook.user.dto;

public record MonthlyRevenueResponse(
    Integer month,
    Long totalRevenue
) {
}