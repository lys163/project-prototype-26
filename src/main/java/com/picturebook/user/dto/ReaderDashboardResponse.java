package com.picturebook.user.dto;

import com.picturebook.global.response.PageResponse;
import com.picturebook.goal.dto.ReadingGoalResponse;
import com.picturebook.reading.dto.MyReadingProgressResponse;

public record ReaderDashboardResponse(
        long totalReadingBookCount,
        long completedBookCount,
        long readingBookCount,
        ReadingGoalResponse monthlyGoal,
        PageResponse<MyReadingProgressResponse> recentReadingProgresses
) {
}
