package com.picturebook.goal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SaveReadingGoalRequest(
    @NotNull(message = "목표 권수는 필수입니다.")
    @Min(value = 1, message = "목표 권수는 1 이상이어야 합니다.")
    @Max(value = 999, message = "목표 권수는 999 이하여야 합니다.")
    Integer targetCount
) {
}