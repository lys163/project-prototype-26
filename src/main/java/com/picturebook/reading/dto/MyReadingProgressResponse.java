package com.picturebook.reading.dto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.picturebook.book.entity.Book;
import com.picturebook.reading.entity.ReadingProgress;

public record MyReadingProgressResponse(
    // 책 ID
    UUID bookId,
    // 책 제목
    String title,
    // 책 표지 이미지 URL
    String coverImageUrl,
    // 작가
    String authorName,
    // 진행도
    Integer progressPercentage,
    // 완독 여부
    boolean isCompleted,
    // 마지막 읽은 날짜 계산 (예: "3일 전", "2시간 전")
    String lastReadAt
) {
    public static MyReadingProgressResponse from(ReadingProgress progress, Book book){
        return new MyReadingProgressResponse(
            book.getId(), 
            book.getTitle(), 
            book.getCoverImageUrl(), 
            book.getAuthorName(), 
            calculatePercentage(progress.getLastReadPageNumber(), book.getTargetPageCount()), 
            progress.isCompleted(), 
            formatRelativeTime(progress.getUpdatedAt())
        );
    }

    public static MyReadingProgressResponse deleted(ReadingProgress progress) {
        return new MyReadingProgressResponse(
            progress.getBookId(),
            "정보를 찾을 수 없는 도서",
            null,
            null,
            0,
            progress.isCompleted(),
            formatRelativeTime(progress.getUpdatedAt())
        );
    }

    private static Integer calculatePercentage(int current, Integer total){
        if (total == null || total <= 0){
            return 0;
        }
        double percentage = ((double) current / total) * 100;
        return (int) Math.min(Math.round(percentage), 100);
    }

    private static String formatRelativeTime(LocalDateTime updatedAt){
        if (updatedAt == null){
            return "기록 없음";
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(updatedAt, now);
        long seconds = duration.getSeconds();
        
        // 60초 미만
        if (seconds < 60) return "방금 전";
        if (seconds < 3600) return (seconds / 60) + "분 전";
        if (seconds < 86400) return (seconds / 3600) + "시간 전";

        long days = duration.toDays();
        if (days < 30) return days + "일 전";

        long months = ChronoUnit.MONTHS.between(updatedAt, now);
        if (months < 12) return months + "개월 전";

        long years = ChronoUnit.YEARS.between(updatedAt, now);
        return years + "년 전";
    }
}
