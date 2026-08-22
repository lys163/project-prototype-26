package com.picturebook.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.picturebook.book.entity.Book;
import com.picturebook.book.enums.VisibilityType;
import com.picturebook.book.repository.BookRepository;
import com.picturebook.global.exception.CustomException;
import com.picturebook.global.exception.ErrorCode;
import com.picturebook.like.repository.BookLikeRepository;
import com.picturebook.purchase.repository.PurchaseRepository;
import com.picturebook.review.repository.ReviewRepository;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookLikeRepository bookLikeRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void ownerCanPublishCompletedBookAsPaid() {
        UUID ownerId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        Book book = completedBook(ownerId);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        bookService.publishAsPaid(ownerId, bookId, 1_000);

        assertThat(book.getVisibility()).isEqualTo(VisibilityType.PAID);
        assertThat(book.getPrice()).isEqualTo(1_000);
        assertThat(book.getShareLinkToken()).isNotBlank();
        assertThat(book.getPublishedAt()).isNotNull();
    }

    @Test
    void anotherUserCannotPublishBookAsPaid() {
        UUID ownerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(completedBook(ownerId)));

        CustomException exception = catchThrowableOfType(
                () -> bookService.publishAsPaid(requesterId, bookId, 1_000),
                CustomException.class
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BOOK_FORBIDDEN);
    }

    @Test
    void missingBookCannotBePublishedAsPaid() {
        UUID bookId = UUID.randomUUID();
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        CustomException exception = catchThrowableOfType(
                () -> bookService.publishAsPaid(UUID.randomUUID(), bookId, 1_000),
                CustomException.class
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BOOK_NOT_FOUND);
    }

    @Test
    void ownerReceivesTwelveZeroMonthsWhenBookHasNoSales() {
        UUID ownerId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        int year = 2026;
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(completedBook(ownerId)));
        when(purchaseRepository.findMonthlySalesCount(
                eq(ownerId), eq(bookId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        var response = bookService.getBookMonthlySales(ownerId, bookId, year);

        assertThat(response.bookId()).isEqualTo(bookId);
        assertThat(response.year()).isEqualTo(year);
        assertThat(response.monthlySales())
                .hasSize(12)
                .allSatisfy(monthlySales -> assertThat(monthlySales.salesCount()).isZero());
    }

    @Test
    void anotherUserCannotViewBookMonthlySales() {
        UUID ownerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(completedBook(ownerId)));

        CustomException exception = catchThrowableOfType(
                () -> bookService.getBookMonthlySales(requesterId, bookId, 2026),
                CustomException.class
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BOOK_SALES_FORBIDDEN);
        verifyNoInteractions(purchaseRepository);
    }

    @Test
    void missingBookMonthlySalesReturnsBookNotFound() {
        UUID bookId = UUID.randomUUID();
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        CustomException exception = catchThrowableOfType(
                () -> bookService.getBookMonthlySales(UUID.randomUUID(), bookId, 2026),
                CustomException.class
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BOOK_NOT_FOUND);
        verifyNoInteractions(purchaseRepository);
    }

    private Book completedBook(UUID ownerId) {
        Book book = Book.builder()
                .userId(ownerId)
                .title("테스트 책")
                .authorName("테스트 작가")
                .build();
        book.startProgress();
        book.complete();
        return book;
    }
}
