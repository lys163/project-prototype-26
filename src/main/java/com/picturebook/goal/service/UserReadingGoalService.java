package com.picturebook.goal.service;


import java.time.LocalDate;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.picturebook.goal.dto.SaveReadingGoalRequest;
import com.picturebook.global.exception.CustomException;
import com.picturebook.global.exception.ErrorCode;
import com.picturebook.goal.dto.ReadingGoalResponse;
import com.picturebook.goal.entity.UserReadingGoal;
import com.picturebook.goal.repository.UserReadingGoalRepository;
import com.picturebook.reading.service.ReadingProgressService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserReadingGoalService {

    private final UserReadingGoalRepository userReadingGoalRepository;
    private final ReadingProgressService readingProgressService;

    // 이번 달 읽기 목표 저장 또는 업데이트
    @Transactional
    public void saveMonthlyReadingGoal(UUID userId, SaveReadingGoalRequest request){
        LocalDate now = LocalDate.now();

        Optional<UserReadingGoal> optionalGoal = userReadingGoalRepository.findByUserIdAndYearAndMonth(userId, now.getYear(), now.getMonthValue());

        if (optionalGoal.isPresent()){
            optionalGoal.get().updateTargetCount(request.targetCount());
            return;
        }
        UserReadingGoal goal = UserReadingGoal.create(userId, now.getYear(), now.getMonthValue(), request.targetCount());

        userReadingGoalRepository.save(goal);
    }

    @Transactional(readOnly = true)
    public ReadingGoalResponse getMonthlyReadingGoal(UUID userId, Integer year, Integer month){

        boolean hasYear = year != null;
        boolean hasMonth = month != null;

        if (hasYear != hasMonth){
            throw new CustomException(ErrorCode.INVALID_GOAL_DATE);
        }

        LocalDate now = LocalDate.now();

        if (!hasYear){
            year = now.getYear();
            month = now.getMonthValue();
        }

        // 완독 수 count
        int completedCount = readingProgressService.countCompletedBooks(userId, year, month);

        Optional<UserReadingGoal> optionalGoal = userReadingGoalRepository.findByUserIdAndYearAndMonth(userId, year, month);

        if (optionalGoal.isEmpty()){
            return new ReadingGoalResponse(null, completedCount);
        }

        UserReadingGoal goal = optionalGoal.get();

        return new ReadingGoalResponse(goal.getTargetCount(), completedCount);
        
        
    }
}
