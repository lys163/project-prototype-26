package com.picturebook.reading.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.picturebook.reading.entity.ReadingProgress;

@Repository
public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, UUID> {
        
    Optional<ReadingProgress> findByUserIdAndBookId(UUID userId, UUID bookId);

    // 전체 조회
    Page<ReadingProgress> findByUserId(UUID userId, Pageable pageable);

    long countByUserId(UUID userId);

    long countByUserIdAndCompletedAtIsNull(UUID userId);

    long countByUserIdAndCompletedAtIsNotNull(UUID userId);

    // 미완독만 조회
    @Query("SELECT r FROM ReadingProgress r WHERE r.userId = :userId AND r.completedAt IS NULL")
    Page<ReadingProgress> findUncompletedByUserId(@Param("userId") UUID userId, Pageable pageable);

    // 월별 완독 수 조회
    long countByUserIdAndCompletedAtGreaterThanEqualAndCompletedAtLessThan(
        UUID userId,
        LocalDateTime startDateTime,
        LocalDateTime nextMonthStartDateTime
    );
} 
