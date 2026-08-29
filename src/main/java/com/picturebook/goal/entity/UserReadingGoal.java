package com.picturebook.goal.entity;

import com.picturebook.global.entity.BaseTimeEntity;
import com.picturebook.global.exception.CustomException;
import com.picturebook.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 사용자의 월간 독서 목표
 * <p>
 * (user_id, year, month) 단위로 1행만 유지됩니다.
 * 같은 달 안에서 목표를 변경하면 동일 행을 갱신합니다.
 * 목표가 없는 달은 행 자체가 존재하지 않습니다(응답에서 null로 표현).
 */
@Entity
@Table(name = "user_reading_goals",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_user_reading_goals_user_year_month",
                        columnNames = {"user_id", "goal_year", "goal_month"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserReadingGoal extends BaseTimeEntity {

    private static final int MIN_TARGET = 1;
    private static final int MAX_TARGET = 999;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 사용자 → users.id */
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "goal_year", nullable = false)
    private Integer year;

    @Column(name = "goal_month", nullable = false)
    private Integer month;

    @Column(name = "target_count", nullable = false)
    private Integer targetCount;

    private UserReadingGoal(UUID userId, int year, int month, int targetCount) {
        validateMonth(month);
        validateTargetCount(targetCount);
        this.userId = userId;
        this.year = year;
        this.month = month;
        this.targetCount = targetCount;
    }

    public static UserReadingGoal create(UUID userId, int year, int month, int targetCount) {
        return new UserReadingGoal(userId, year, month, targetCount);
    }

    public void updateTargetCount(int targetCount) {
        validateTargetCount(targetCount);
        this.targetCount = targetCount;
    }

    private static void validateMonth(int month) {
        if (month < 1 || month > 12) {
            throw new CustomException(ErrorCode.INVALID_GOAL_MONTH);
        }
    }

    private static void validateTargetCount(int targetCount) {
        if (targetCount < MIN_TARGET || targetCount > MAX_TARGET) {
            throw new CustomException(ErrorCode.INVALID_GOAL_VALUE);
        }
    }
}
