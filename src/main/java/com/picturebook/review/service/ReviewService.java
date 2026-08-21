package com.picturebook.review.service;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.picturebook.book.entity.Book;
import com.picturebook.book.enums.VisibilityType;
import com.picturebook.book.repository.BookRepository;
import com.picturebook.global.exception.CustomException;
import com.picturebook.global.exception.ErrorCode;
import com.picturebook.global.response.PageResponse;
import com.picturebook.review.dto.ReviewDeleteResponse;
import com.picturebook.review.dto.ReviewRequest;
import com.picturebook.review.dto.ReviewResponse;
import com.picturebook.review.entity.Review;
import com.picturebook.review.repository.ReviewRepository;
import com.picturebook.user.entity.User;
import com.picturebook.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getBookReviews(UUID bookId, UUID currentUserId, int page, int size) {
        Book book = findPublicBook(bookId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewRepository.findByBookId(bookId, pageable);
        Map<UUID, User> users = findUsersByReviews(reviews);

        Page<ReviewResponse> responses = reviews.map(review -> ReviewResponse.from(
                review,
                book.getTitle(),
                getNickname(users, review.getUserId()),
                currentUserId
        ));
        return PageResponse.from(responses);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getMyReviews(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewRepository.findByUserId(userId, pageable);
        Map<UUID, Book> books = findBooksByReviews(reviews);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Page<ReviewResponse> responses = reviews.map(review -> ReviewResponse.from(
                review,
                getBookTitle(books, review.getBookId()),
                user.getNickname(),
                userId
        ));
        return PageResponse.from(responses);
    }

    @Transactional
    public ReviewResponse createReview(UUID bookId, UUID userId, ReviewRequest request) {
        Book book = findPublicBook(bookId);
        if (book.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.CANNOT_REVIEW_OWN_BOOK);
        }
        if (reviewRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new CustomException(ErrorCode.ALREADY_REVIEWED);
        }

        Review review = reviewRepository.save(Review.create(userId, bookId, request.rating(), request.content()));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return ReviewResponse.from(review, book.getTitle(), user.getNickname(), userId);
    }

    @Transactional
    public ReviewResponse updateReview(UUID reviewId, UUID userId, ReviewRequest request) {
        Review review = findMyReview(reviewId, userId);
        Book book = bookRepository.findById(review.getBookId())
                .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        review.update(request.rating(), request.content());

        return ReviewResponse.from(review, book.getTitle(), user.getNickname(), userId);
    }

    @Transactional
    public ReviewDeleteResponse deleteReview(UUID reviewId, UUID userId) {
        Review review = findMyReview(reviewId, userId);
        UUID bookId = review.getBookId();

        reviewRepository.delete(review);

        return new ReviewDeleteResponse(reviewId, bookId, true);
    }

    private Book findPublicBook(UUID bookId) {
        return bookRepository.findByIdAndVisibility(bookId, VisibilityType.PUBLIC)
                .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));
    }

    private Review findMyReview(UUID reviewId, UUID userId) {
        return reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
    }

    private Map<UUID, User> findUsersByReviews(Page<Review> reviews) {
        return userRepository.findAllById(
                        reviews.getContent().stream()
                                .map(Review::getUserId)
                                .distinct()
                                .toList()
                ).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private Map<UUID, Book> findBooksByReviews(Page<Review> reviews) {
        return bookRepository.findAllById(
                        reviews.getContent().stream()
                                .map(Review::getBookId)
                                .distinct()
                                .toList()
                ).stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));
    }

    private String getNickname(Map<UUID, User> users, UUID userId) {
        User user = users.get(userId);
        return user != null ? user.getNickname() : null;
    }

    private String getBookTitle(Map<UUID, Book> books, UUID bookId) {
        Book book = books.get(bookId);
        return book != null ? book.getTitle() : null;
    }

    public Double getAuthorAverageRating(UUID userId) {
        return reviewRepository.findAuthorAverageRating(userId);
    }
}
