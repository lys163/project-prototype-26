package com.picturebook.like.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.picturebook.book.entity.Book;
import com.picturebook.book.enums.VisibilityType;
import com.picturebook.book.repository.BookRepository;
import com.picturebook.global.exception.CustomException;
import com.picturebook.global.exception.ErrorCode;
import com.picturebook.like.dto.BookLikeResponse;
import com.picturebook.like.entity.BookLike;
import com.picturebook.like.repository.BookLikeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookLikeService {

    private final BookRepository bookRepository;
    private final BookLikeRepository bookLikeRepository;

    @Transactional
    public BookLikeResponse likeBook(UUID bookId, UUID userId) {
        Book book = findPublicBook(bookId);

        if (!bookLikeRepository.existsByUserIdAndBookId(userId, book.getId())) {
            bookLikeRepository.save(BookLike.create(userId, book.getId()));
        }
        return buildResponse(book.getId(), userId);
    }

    @Transactional
    public BookLikeResponse unlikeBook(UUID bookId, UUID userId) {
        findPublicBook(bookId);

        bookLikeRepository.findByUserIdAndBookId(userId, bookId)
                .ifPresent(bookLikeRepository::delete);
        return buildResponse(bookId, userId);
    }

    @Transactional(readOnly = true)
    public BookLikeResponse getBookLike(UUID bookId, UUID userId) {
        findPublicBook(bookId);
        return buildResponse(bookId, userId);
    }

    private BookLikeResponse buildResponse(UUID bookId, UUID userId) {
        long likeCount = bookLikeRepository.countByBookId(bookId);
        boolean likedByMe = userId != null && bookLikeRepository.existsByUserIdAndBookId(userId, bookId);
        return new BookLikeResponse(bookId, likeCount, likedByMe);
    }

    private Book findPublicBook(UUID bookId) {
        return bookRepository.findByIdAndVisibility(bookId, VisibilityType.PUBLIC)
                .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));
    }
}
