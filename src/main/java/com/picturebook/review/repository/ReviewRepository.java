package com.picturebook.review.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.picturebook.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByBookId(UUID bookId, Pageable pageable);

    Page<Review> findByUserId(UUID userId, Pageable pageable);

    boolean existsByUserIdAndBookId(UUID userId, UUID bookId);

    Optional<Review> findByIdAndUserId(UUID id, UUID userId);

    // 작품별 평점 조회
    @Query("""
        SELECT r.bookId, AVG(r.rating)
        FROM Review r
        WHERE r.bookId IN :bookIds
        GROUP BY r.bookId
    """)
    List<Object[]> averageRatings(List<UUID> bookIds);

    // 작가 평점 평균 조회
    @Query("""
        SELECT COALESCE(AVG(r.rating), 0.0)
        FROM Review r
        JOIN Book b ON b.id = r.bookId
        WHERE b.userId = :userId
        AND b.status = com.picturebook.book.enums.BookStatus.COMPLETED
        AND b.visibility !=
        com.picturebook.book.enums.VisibilityType.PRIVATE
    """)
    Double findAuthorAverageRating(UUID userId);
}
