package com.picturebook.user.dto;

import java.util.List;

public record YearlyRevenueResponse(
    Integer year,
    List<MonthlyRevenueResponse> monthlyRevenues
) {
    
}
