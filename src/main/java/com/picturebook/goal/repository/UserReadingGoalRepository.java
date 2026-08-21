package com.picturebook.goal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.picturebook.goal.entity.UserReadingGoal;

@Repository
public interface UserReadingGoalRepository extends JpaRepository<UserReadingGoal, UUID> {

    Optional<UserReadingGoal> findByUserIdAndYearAndMonth(UUID userId, int year, int month);
}
