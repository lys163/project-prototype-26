package com.picturebook.ranking.service;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.picturebook.book.enums.VisibilityType;
import com.picturebook.global.exception.CustomException;
import com.picturebook.global.exception.ErrorCode;
import com.picturebook.ranking.dto.PopularAuthorResponse;
import com.picturebook.ranking.dto.PopularBookResponse;
import com.picturebook.ranking.dto.ProlificAuthorResponse;
import com.picturebook.ranking.repository.RankingQueryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RankingService {
    private final RankingQueryRepository rankingQueryRepository;

    private static final int RANKING_LIMIT = 3;

    // 둘다 null이면 현재 년월 (월간)
    private YearMonth getYearMonth(Integer year, Integer month){
        if (year == null && month == null){
            return YearMonth.now();
        }
        if (year == null || month == null){
            throw new CustomException(ErrorCode.INVALID_DATE_FORMAT);
        }
        try{
            return YearMonth.of(year, month);
        } catch (DateTimeException e){
            throw new CustomException(ErrorCode.INVALID_DATE_FORMAT);
        }
    }
    // 3개가 모두 null이면 현재 날짜 (주간)
    private LocalDate getLocalDate(Integer year, Integer month, Integer day){
        if (year == null && month == null && day == null){
            return LocalDate.now();
        }
        if (year == null || month == null || day == null){
            throw new CustomException(ErrorCode.INVALID_DATE_FORMAT);
        }
        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            throw new CustomException(ErrorCode.INVALID_DATE_FORMAT);
        }
    }

    private LocalDateTime getStartOfMonth(YearMonth yearMonth){
        return yearMonth
            .atDay(1)
            .atStartOfDay();
    }

    private LocalDateTime getNextMonthStart(YearMonth yearMonth){
        return yearMonth
            .plusMonths(1)
            .atDay(1)
            .atStartOfDay();
    }

    private LocalDateTime getStartOfWeek(LocalDate date){
        return date
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .atStartOfDay();
    }

    private LocalDateTime getNextWeekStart(LocalDate date){
        return date
            .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
            .atStartOfDay();
    }

    @FunctionalInterface
    private interface RankApplier<T> {
        T apply(T item, int rank);
    }

    private <T> List<T> applyRank(List<T> list, RankApplier<T> applier){
        return IntStream.range(0, list.size())
            .mapToObj(i->applier.apply(list.get(i), i+1))
            .toList();
    }

    
    // 월별 가장 많은 책 출간 작가 (public 책만, 출간일 기준 우선순위)
    public List<ProlificAuthorResponse> getMonthlyProlificAuthors(Integer year, Integer month) {
        YearMonth yearMonth = getYearMonth(year, month);
        LocalDateTime startOfMonth = getStartOfMonth(yearMonth);
        LocalDateTime nextMonthStart = getNextMonthStart(yearMonth);

        List<ProlificAuthorResponse> authors = rankingQueryRepository
            .findTopAuthorsByBookCount(
                startOfMonth, 
                nextMonthStart,
                VisibilityType.PUBLIC,
                RANKING_LIMIT
            );

        return applyRank(
            authors, (author, rank) -> author.withRank(rank)
        );
    }
    // 월별 가장 많은 좋아요를 받은 작가 (public 책만, 좋아요 생성일 기준 우선순위)
    public List<PopularAuthorResponse> getMonthlyPopularAuthors(Integer year, Integer month) {
        YearMonth yearMonth = getYearMonth(year, month);
        LocalDateTime startOfMonth = getStartOfMonth(yearMonth);
        LocalDateTime nextMonthStart = getNextMonthStart(yearMonth);

        List<PopularAuthorResponse> authors = rankingQueryRepository
            .findTopAuthorsByTotalLikes(
                startOfMonth, 
                nextMonthStart,
                VisibilityType.PUBLIC,
                RANKING_LIMIT
            );
        
        return applyRank(
            authors, (author, rank) -> author.withRank(rank)
        );
    }

    // 월별 가장 많은 좋아요를 받은 책 (public 책만, 좋아요 생성일 기준 우선순위)
    public List<PopularBookResponse> getMonthlyPopularBooks(Integer year, Integer month) {
        YearMonth yearMonth = getYearMonth(year, month);
        LocalDateTime startOfMonth = getStartOfMonth(yearMonth);
        LocalDateTime nextMonthStart = getNextMonthStart(yearMonth);

        List<PopularBookResponse> results = rankingQueryRepository.findTopBooksByLikes(
                startOfMonth,
                nextMonthStart,
                VisibilityType.PUBLIC,
                RANKING_LIMIT
        );
        
        return applyRank(results, (book, rank) -> book.withRank(rank));
    }

    // 주별 가장 많은 책 출간 작가 (public 책만, 출간일 기준 우선순위)
    public List<ProlificAuthorResponse> getWeeklyProlificAuthors(Integer year, Integer month, Integer day) {
        LocalDate date = getLocalDate(year, month, day);
        LocalDateTime startOfWeek = getStartOfWeek(date);
        LocalDateTime nextOfWeek = getNextWeekStart(date);

        List<ProlificAuthorResponse> authors = rankingQueryRepository
            .findTopAuthorsByBookCount(
                startOfWeek, 
                nextOfWeek,
                VisibilityType.PUBLIC,
                RANKING_LIMIT
            );

        return applyRank(
            authors, (author, rank) -> author.withRank(rank)
        );
    }

    // 주별 가장 많은 좋아요를 받은 작가 (public 책만, 좋아요 생성일 기준 우선순위)
    public List<PopularAuthorResponse> getWeeklyPopularAuthors(Integer year, Integer month, Integer day) {
        LocalDate date = getLocalDate(year, month, day);
        LocalDateTime startOfWeek = getStartOfWeek(date);
        LocalDateTime nextOfWeek = getNextWeekStart(date);

        List<PopularAuthorResponse> authors = rankingQueryRepository
            .findTopAuthorsByTotalLikes(
                startOfWeek, 
                nextOfWeek,
                VisibilityType.PUBLIC,
                RANKING_LIMIT
            );
        
        return applyRank(
            authors, (author, rank) -> author.withRank(rank)
        );
    }

    // 주별 가장 많은 좋아요를 받은 책 (public 책만, 좋아요 생성일 기준 우선순위)
    public List<PopularBookResponse> getWeeklyPopularBooks(Integer year, Integer month, Integer day) {
        LocalDate date = getLocalDate(year, month, day);
        LocalDateTime startOfWeek = getStartOfWeek(date);
        LocalDateTime nextOfWeek = getNextWeekStart(date);

        List<PopularBookResponse> results = rankingQueryRepository.findTopBooksByLikes(
                startOfWeek,
                nextOfWeek,
                VisibilityType.PUBLIC,
                RANKING_LIMIT
        );
        
        return applyRank(results, (book, rank) -> book.withRank(rank));
    }
}
