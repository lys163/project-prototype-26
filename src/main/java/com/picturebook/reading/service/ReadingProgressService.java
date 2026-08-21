package com.picturebook.reading.service;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.picturebook.book.entity.Book;
import com.picturebook.book.repository.BookRepository;
import com.picturebook.global.exception.CustomException;
import com.picturebook.global.exception.ErrorCode;
import com.picturebook.global.response.PageResponse;
import com.picturebook.reading.dto.MyReadingProgressResponse;
import com.picturebook.reading.dto.ReadingProgressResponse;
import com.picturebook.reading.entity.ReadingProgress;
import com.picturebook.reading.repository.ReadingProgressRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReadingProgressService {
    
    private final ReadingProgressRepository readingProgressRepository;
    private final BookRepository bookRepository;

    // 책 읽기 진행 상황 업데이트
    @Transactional
    public void updateProgress(UUID userId, UUID bookId, Integer lastReadPageNumber){
        
        ReadingProgress progress = getOrCreateProgress(userId, bookId, lastReadPageNumber);

        progress.updatePage(lastReadPageNumber);

        readingProgressRepository.save(progress);
    }
    
    // 책 읽기 완료 처리(완독 시각 기록, 페이지 번호 업데이트)
    @Transactional
    public void completeProgress(UUID userId, UUID bookId, Integer lastReadPageNumber){
        
        ReadingProgress progress = getOrCreateProgress(userId, bookId, lastReadPageNumber);

        progress.updatePage(lastReadPageNumber);

        if (!progress.isCompleted()) {
            progress.markCompleted();
        }

        readingProgressRepository.save(progress);
    }

    private ReadingProgress getOrCreateProgress(UUID userId, UUID bookId, Integer lastReadPageNumber) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));

        if (book.getTargetPageCount() != null && lastReadPageNumber > book.getTargetPageCount()){
            throw new CustomException(ErrorCode.PAGE_BAD_REQUEST);
        }

        return readingProgressRepository.findByUserIdAndBookId(userId, bookId)
            .orElseGet(() -> ReadingProgress.create(userId, bookId, lastReadPageNumber));
    }

    @Transactional(readOnly = true)
    public ReadingProgressResponse getProgress(UUID userId, UUID bookId){
        
        bookRepository.findById(bookId)
            .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));

        return readingProgressRepository.findByUserIdAndBookId(userId, bookId)
            .map(ReadingProgressResponse::from)
            .orElseGet(() -> new ReadingProgressResponse(1, false));
    }

    @Transactional(readOnly = true)
    public PageResponse<MyReadingProgressResponse> getMyReadingProgresses(
        UUID userId, 
        boolean includeCompleted, 
        Pageable pageable
    ){
        Page<ReadingProgress> progressPage;

        if (!includeCompleted){
            progressPage = readingProgressRepository.findUncompletedByUserId(userId, pageable);
        }else{
            progressPage = readingProgressRepository.findByUserId(userId, pageable);
        }

        if (progressPage.isEmpty()){
            return PageResponse.from(Page.empty());
        }

        List<UUID> bookIds = progressPage.getContent().stream()
            .map(ReadingProgress::getBookId)
            .toList();

        List<Book> books = bookRepository.findAllById(bookIds);
        
        Map<UUID, Book> bookMap = books.stream()
            .collect(Collectors.toMap(Book::getId, book -> book));

        Page<MyReadingProgressResponse> dtoPage = progressPage.map(progress -> {
            Book book = bookMap.get(progress.getBookId());
            if (book==null){
                return MyReadingProgressResponse.deleted(progress);
            }
            return MyReadingProgressResponse.from(progress, book);
        });

        return PageResponse.from(dtoPage);
    }

    @Transactional(readOnly = true)
    public long countTotalReadingBooks(UUID userId) {
        return readingProgressRepository.countByUserId(userId);
    }

    @Transactional(readOnly = true)
    public long countReadingBooks(UUID userId) {
        return readingProgressRepository.countByUserIdAndCompletedAtIsNull(userId);
    }

    @Transactional(readOnly = true)
    public long countAllCompletedBooks(UUID userId) {
        return readingProgressRepository.countByUserIdAndCompletedAtIsNotNull(userId);
    }

    // 완독 수 조회
    @Transactional(readOnly = true)
    public int countCompletedBooks(UUID userId, int year, int month) {

        YearMonth yearMonth;
        try{
            yearMonth = YearMonth.of(year, month);
        }catch(DateTimeException e){
            throw new CustomException(ErrorCode.INVALID_GOAL_DATE);
        }

        LocalDateTime startDateTime = yearMonth
            .atDay(1)
            .atStartOfDay();

        LocalDateTime nextMonthStartDateTime = yearMonth
            .plusMonths(1)
            .atDay(1)
            .atStartOfDay();

        // 시작일 <= completedAt < 다음달 시작일
        return (int) readingProgressRepository.countByUserIdAndCompletedAtGreaterThanEqualAndCompletedAtLessThan(
                        userId,
                        startDateTime,
                        nextMonthStartDateTime
                );
    }

}
