package com.picturebook.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.picturebook.book.entity.Book;
import com.picturebook.book.repository.BookRepository;
import com.picturebook.global.exception.CustomException;
import com.picturebook.global.exception.ErrorCode;
import com.picturebook.review.dto.ReviewDeleteResponse;
import com.picturebook.review.dto.ReviewRequest;
import com.picturebook.review.dto.ReviewResponse;
import com.picturebook.review.entity.Review;
import com.picturebook.review.repository.ReviewRepository;
import com.picturebook.user.entity.User;
import com.picturebook.user.enums.SocialProvider;
import com.picturebook.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void ownerCanUpdateReview() {
        UUID ownerId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        Review review = Review.create(ownerId, bookId, 3, "수정 전");
        Book book = Book.builder().userId(UUID.randomUUID()).title("테스트 책").build();
        User user = User.builder()
                .nickname("테스트 사용자")
                .provider(SocialProvider.KAKAO)
                .providerId("test-provider-id")
                .build();
        when(reviewRepository.findByIdAndUserId(reviewId, ownerId)).thenReturn(Optional.of(review));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(user));

        ReviewResponse response = reviewService.updateReview(
                reviewId,
                ownerId,
                new ReviewRequest(5, "수정 후")
        );

        assertThat(response.rating()).isEqualTo(5);
        assertThat(response.content()).isEqualTo("수정 후");
        assertThat(response.mine()).isTrue();
    }

    @Test
    void anotherUserCannotUpdateReview() {
        UUID reviewId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        when(reviewRepository.findByIdAndUserId(reviewId, requesterId)).thenReturn(Optional.empty());

        CustomException exception = catchThrowableOfType(
                () -> reviewService.updateReview(reviewId, requesterId, new ReviewRequest(5, "수정")),
                CustomException.class
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
    }

    @Test
    void ownerCanDeleteReview() {
        UUID ownerId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        Review review = Review.create(ownerId, bookId, 4, "삭제 대상");
        when(reviewRepository.findByIdAndUserId(reviewId, ownerId)).thenReturn(Optional.of(review));

        ReviewDeleteResponse response = reviewService.deleteReview(reviewId, ownerId);

        assertThat(response.reviewId()).isEqualTo(reviewId);
        assertThat(response.bookId()).isEqualTo(bookId);
        assertThat(response.deleted()).isTrue();
        verify(reviewRepository).delete(review);
    }

    @Test
    void anotherUserCannotDeleteReview() {
        UUID reviewId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        when(reviewRepository.findByIdAndUserId(reviewId, requesterId)).thenReturn(Optional.empty());

        CustomException exception = catchThrowableOfType(
                () -> reviewService.deleteReview(reviewId, requesterId),
                CustomException.class
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
    }
}
