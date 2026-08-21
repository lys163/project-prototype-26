package com.picturebook.goal.dto;

public record ReadingGoalResponse(
    // 이번달 목표 완독 수
    Integer targetCount,
    // 이번달 완독 수
    Integer completedCount,
    // 이번달 목표 달성률 (예: 75)
    Integer achievementPercentage
) {
    public ReadingGoalResponse(Integer targetCount, Integer completedCount) {
        this(targetCount, completedCount, calculatePercentage(completedCount, targetCount));
    }

    private static Integer calculatePercentage(int completedCount, Integer targetCount){
        
        if (targetCount == null || targetCount <= 0){
            return 0;
        }

        double percentage = ((double) completedCount / targetCount) * 100;
        return (int) Math.min(Math.round(percentage), 100);
    }
}
