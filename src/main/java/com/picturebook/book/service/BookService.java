package com.picturebook.book.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.picturebook.book.dto.AuthorBookResponse;
import com.picturebook.book.dto.BookDetailResponse;
import com.picturebook.book.dto.BookListResponse;
import com.picturebook.book.dto.BookYearlySalesResponse;
import com.picturebook.book.dto.MonthlySalesResponse;
import com.picturebook.book.dto.BookPerformanceResponse;
import com.picturebook.book.dto.BestsellerBookResponse;
import com.picturebook.book.dto.BestsellerHighlightResponse;
import com.picturebook.book.dto.BestsellerItemResponse;
import com.picturebook.book.dto.MyBookResponse;
import com.picturebook.book.entity.Book;
import com.picturebook.book.enums.BestsellerPeriod;
import com.picturebook.book.enums.BookStatus;
import com.picturebook.book.enums.VisibilityType;
import com.picturebook.book.repository.BookRepository;
import com.picturebook.global.exception.CustomException;
import com.picturebook.global.exception.ErrorCode;
import com.picturebook.global.response.PageResponse;
import com.picturebook.like.dto.LikedBookResponse;
import com.picturebook.like.repository.BookLikeRepository;
import com.picturebook.purchase.repository.PurchaseRepository;
import com.picturebook.review.repository.ReviewRepository;
import com.picturebook.user.dto.UserCountResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookLikeRepository bookLikeRepository;
    private final ReviewRepository reviewRepository;
    private final PurchaseRepository purchaseRepository;

    @Transactional(readOnly = true)
    public PageResponse<BookListResponse> getPublicBookList(int page, int size, UUID userId, Integer categoryId) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("publishedAt").nullsLast())
        );

        // 둘러보기 카탈로그: 무료(PUBLIC) + 유료(PAID) 모두 노출
        List<VisibilityType> listedVisibilities = List.of(VisibilityType.PUBLIC, VisibilityType.PAID);

        Page<Book> books = (categoryId != null)
                ? bookRepository.findByVisibilityInAndCategoryId(listedVisibilities, categoryId, pageable)
                : bookRepository.findByVisibilityIn(listedVisibilities, pageable);

        //현재 페이지 책 id 목록 추출
        List<UUID> bookIds = books.map(Book::getId).getContent();

        // 로그인 사용자인 경우 좋아요한 책 ID 조회
        Set<UUID> likedBookIds = (userId != null)
                ? bookLikeRepository.findBookIdsByUserIdAndBookIdIn(userId, bookIds)
                : Collections.emptySet();

        Page<BookListResponse> result = books.map(book -> BookListResponse.builder()
                .bookId(book.getId())
                .title(book.getTitle())
                .coverImageUrl(book.getCoverImageUrl())
                .authorName(book.getAuthorName())
                .price(book.getPrice())
                .liked(likedBookIds.contains(book.getId()))
                .build());

        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public PageResponse<MyBookResponse> getMyBookList(int page, int size, UUID userId, BookStatus status) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Book> books = (status != null)
                ? bookRepository.findByUserIdAndStatus(userId, status, pageable)
                : bookRepository.findByUserIdAndStatusIn(
                        userId,
                        List.of(BookStatus.DRAFT, BookStatus.IN_PROGRESS, BookStatus.COMPLETED),
                        pageable
                );

        Page<MyBookResponse> result = books.map(book -> MyBookResponse.builder()
                .bookId(book.getId())
                .title(book.getTitle())
                .authorName(book.getAuthorName())
                .coverImageUrl(book.getCoverImageUrl())
                .status(book.getStatus())
                .visibility(book.getVisibility())
                .createdAt(book.getCreatedAt())
                .build());

        return PageResponse.from(result);
    }
    @Transactional(readOnly = true)
    public PageResponse<LikedBookResponse> getMyLikedBookList(int page, int size, UUID userId) {

        Pageable pageable = PageRequest.of(page, size);

        Page<LikedBookResponse> likedBooks = bookLikeRepository.findLikedBooksByUserIdAndVisibilityIn(
                userId,
                List.of(VisibilityType.PUBLIC, VisibilityType.PAID),
                pageable
        );

        return PageResponse.from(likedBooks);
    }

    @Transactional(readOnly = true)
    public PageResponse<BestsellerBookResponse> getBestsellers(int page, int size, UUID userId, Integer categoryId) {

        Pageable pageable = PageRequest.of(page, size);

        Page<BestsellerBookResponse> bestsellers = bookRepository.findBestsellers(categoryId, pageable);

        List<UUID> bookIds = bestsellers.getContent().stream()
                .map(BestsellerBookResponse::bookId)
                .toList();

        Set<UUID> likedBookIds = (userId != null && !bookIds.isEmpty())
                ? bookLikeRepository.findBookIdsByUserIdAndBookIdIn(userId, bookIds)
                : Collections.emptySet();

        AtomicInteger rank = new AtomicInteger(page * size + 1);
        Page<BestsellerBookResponse> responsePage = bestsellers.map(book ->
                book.withRankAndLiked(
                        rank.getAndIncrement(),
                        likedBookIds.contains(book.bookId())
                )
        );

        return PageResponse.from(responsePage);
    }

    // 기간별(주간/월간) 베스트셀러 Top 3 (판매 건수 기준)
    @Transactional(readOnly = true)
    public BestsellerHighlightResponse getBestsellerHighlights(BestsellerPeriod period) {

        LocalDate now = LocalDate.now();

        LocalDateTime start;
        LocalDateTime end;
        if (period == BestsellerPeriod.WEEKLY) {
            start = now.with(DayOfWeek.MONDAY).atStartOfDay();
            end = start.plusWeeks(1);
        } else {
            start = now.withDayOfMonth(1).atStartOfDay();
            end = start.plusMonths(1);
        }

        Pageable top3 = PageRequest.of(0, 3);

        List<BestsellerItemResponse> items = bookRepository.findTopBestsellers(start, end, top3);

        return new BestsellerHighlightResponse(period, items);
    }

    // 유료 출판: 본인 소유 책을 PAID로 전환하고 가격·공유 토큰·출판 시각 설정
    @Transactional
    public void publishAsPaid(UUID userId, UUID bookId, int price) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));

        if (!book.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.BOOK_FORBIDDEN);
        }

        if (book.getStatus() != BookStatus.COMPLETED) {
            throw new CustomException(ErrorCode.BOOK_NOT_COMPLETED);
        }

        String shareLinkToken = UUID.randomUUID().toString().replace("-", "");
        book.publishAsPaid(price, shareLinkToken);
    }

    // 유료 책 구매 전 미리보기로 노출할 페이지 수
    private static final int PAID_PREVIEW_PAGE_COUNT = 3;

    @Transactional
    public BookDetailResponse getBookDetail(UUID bookId, UUID userId) {

        Book book = bookRepository.findByIdAndVisibilityInWithPages(
                        bookId, List.of(VisibilityType.PUBLIC, VisibilityType.PAID))
                .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));

        // 유료 책은 본인(작가) 또는 구매자만 전체 열람, 그 외에는 앞 몇 페이지만 미리보기
        boolean purchased = false;
        boolean locked = false;
        if (book.getVisibility() == VisibilityType.PAID) {
            boolean owner = userId != null && book.getUserId().equals(userId);
            purchased = userId != null && purchaseRepository.existsByUserIdAndBookId(userId, bookId);
            locked = !(owner || purchased);
        }

        bookRepository.incrementViewCount(bookId);

        return BookDetailResponse.from(book, locked, purchased, PAID_PREVIEW_PAGE_COUNT);
    }

    // 작가 공개 작품 목록 (완성된 공개/유료 작품)
    @Transactional(readOnly = true)
    public PageResponse<AuthorBookResponse> getAuthorPublicBooks(UUID authorId, int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("publishedAt").nullsLast())
        );

        Page<AuthorBookResponse> books = bookRepository.findByUserIdAndStatusAndVisibilityIn(
                authorId,
                BookStatus.COMPLETED,
                List.of(VisibilityType.PUBLIC, VisibilityType.PAID),
                pageable
        ).map(book -> new AuthorBookResponse(
                book.getId(),
                book.getTitle(),
                book.getCoverImageUrl(),
                book.getVisibility(),
                book.getPrice(),
                book.getPublishedAt()
        ));

        return PageResponse.from(books);
    }

    @Transactional(readOnly = true)
    public UserCountResponse getMyCreatedBookCount(UUID userId) {
        return new UserCountResponse(bookRepository.countByUserId(userId));
    }

    @Transactional(readOnly = true)
    public UserCountResponse getMySharedBookCount(UUID userId) {
        long count = bookRepository.countByUserIdAndVisibilityIn(
                userId,
                List.of(VisibilityType.PUBLIC, VisibilityType.LINK_SHARED)
        );
        return new UserCountResponse(count);
    }

    @Transactional(readOnly = true)
    public UserCountResponse getMyReceivedLikesCount(UUID userId) {
        long count = bookLikeRepository.countReceivedLikesByUserIdAndVisibilityIn(
                userId,
                List.of(VisibilityType.PUBLIC, VisibilityType.LINK_SHARED)
        );
        return new UserCountResponse(count);
    }

    // 작품 별 성과
    @Transactional(readOnly = true)
    public PageResponse<BookPerformanceResponse> getBookPerformance(UUID userId, Pageable pageable) {

        Page<Book> bookPage = bookRepository.findPerformanceBooks(
            userId,
            pageable
        );

        List<Book> books = bookPage.getContent();

        List<UUID> bookIds = books.stream()
                .map(Book::getId)
                .toList();

        if (bookIds.isEmpty()) {
            Page<BookPerformanceResponse> emptyPage =
                new PageImpl<>(
                    List.of(),
                    pageable,
                    0
                );
            return PageResponse.from(emptyPage);
        }

        // 좋아요 수
        List<Object[]> likeCounts = bookLikeRepository.countLikes(bookIds);

        Map<UUID, Long> likeCountMap = likeCounts.stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> row[1] != null ? ((Number) row[1]).longValue() : 0L
                ));
        // 평점
        List<Object[]> averageRatings = reviewRepository.averageRatings(bookIds);

        Map<UUID, Double> averageRatingMap = averageRatings.stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> row[1] != null ? ((Number) row[1]).doubleValue() : 0.0
                ));
        // 수익
        List<Object[]> revenues = purchaseRepository.sumRevenue(bookIds);
        
        Map<UUID, Long> revenueMap = revenues.stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> row[1] != null ? ((Number) row[1]).longValue() : 0L
                ));
        
        List<BookPerformanceResponse> result = books.stream()
                .map(book -> new BookPerformanceResponse(
                        book.getId(),
                        book.getTitle(),
                        book.getVisibility(),
                        book.getViewCount(),
                        likeCountMap.getOrDefault(book.getId(), 0L),
                        averageRatingMap.getOrDefault(book.getId(), 0.0),
                        revenueMap.getOrDefault(book.getId(), 0L)
                ))
                .toList();
        Page<BookPerformanceResponse> responsePage =
                new PageImpl<>(
                        result,
                        pageable,
                        bookPage.getTotalElements()
                );

        return PageResponse.from(responsePage);
    }
    
    // 작품별 월 판매 건수 추이 (특정 작품의 연도별 1~12월 판매 건수)
    @Transactional(readOnly = true)
    public BookYearlySalesResponse getBookMonthlySales(UUID userId, UUID bookId, Integer year) {

        if (year == null) {
            year = LocalDate.now().getYear();
        }

        LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime end = start.plusYears(1);

        Map<Integer, Long> salesMap = purchaseRepository
                .findMonthlySalesCount(userId, bookId, start, end)
                .stream()
                .collect(Collectors.toMap(
                        MonthlySalesResponse::month,
                        MonthlySalesResponse::salesCount
                ));

        List<MonthlySalesResponse> monthlySales = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            monthlySales.add(new MonthlySalesResponse(
                    month,
                    salesMap.getOrDefault(month, 0L)
            ));
        }

        return new BookYearlySalesResponse(bookId, year, monthlySales);
    }

    @Transactional(readOnly = true)
    public Long countTotalPublishedBooks(UUID userId) {
        return bookRepository.countPublishedBooks(userId);
    }

    @Transactional(readOnly = true)
    public Long countInProgressBooks(UUID userId) {
        return bookRepository.countInProgressBooks(userId);
    }
}
