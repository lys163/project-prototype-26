package com.picturebook.book.repository;

import com.picturebook.book.entity.Book;
import com.picturebook.book.dto.BestsellerBookResponse;
import com.picturebook.book.dto.BestsellerItemResponse;
import com.picturebook.book.enums.BookStatus;
import com.picturebook.book.enums.VisibilityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {

    Page<Book> findByVisibilityIn(Collection<VisibilityType> visibilities, Pageable pageable);

    Page<Book> findByVisibilityInAndCategoryId(Collection<VisibilityType> visibilities, Integer categoryId, Pageable pageable);

    Optional<Book> findByIdAndVisibility(UUID id, VisibilityType visibility);

    Page<Book> findByUserIdAndStatus(UUID userId, BookStatus status, Pageable pageable);

    Page<Book> findByUserIdAndStatusIn(UUID userId, Collection<BookStatus> statuses, Pageable pageable);

    long countByUserId(UUID userId);

    long countByUserIdAndVisibilityIn(UUID userId, Collection<VisibilityType> visibilities);

    Page<Book> findByUserIdAndStatusAndVisibilityIn(
            UUID userId, BookStatus status, Collection<VisibilityType> visibilities, Pageable pageable);

    @Query(
        value = """
            SELECT new com.picturebook.book.dto.BestsellerBookResponse(
                b.id,
                b.title,
                b.coverImageUrl,
                b.authorName,
                b.price,
                COUNT(p.id),
                COALESCE(SUM(p.price), 0),
                0,
                false
            )
            FROM Purchase p
            JOIN Book b ON b.id = p.bookId
            WHERE b.status = com.picturebook.book.enums.BookStatus.COMPLETED
            AND b.visibility = com.picturebook.book.enums.VisibilityType.PAID
            AND (:categoryId IS NULL OR b.categoryId = :categoryId)
            GROUP BY b.id, b.title, b.coverImageUrl, b.authorName, b.price
            ORDER BY COUNT(p.id) DESC, COALESCE(SUM(p.price), 0) DESC, MAX(p.createdAt) DESC
        """,
        countQuery = """
            SELECT COUNT(DISTINCT b.id)
            FROM Purchase p
            JOIN Book b ON b.id = p.bookId
            WHERE b.status = com.picturebook.book.enums.BookStatus.COMPLETED
            AND b.visibility = com.picturebook.book.enums.VisibilityType.PAID
            AND (:categoryId IS NULL OR b.categoryId = :categoryId)
        """
    )
    Page<BestsellerBookResponse> findBestsellers(@Param("categoryId") Integer categoryId, Pageable pageable);

    // 기간별 판매 건수 상위 베스트셀러 (limit은 Pageable로 제어)
    @Query("""
        SELECT new com.picturebook.book.dto.BestsellerItemResponse(
            b.id,
            b.title,
            b.coverImageUrl,
            b.authorName,
            COUNT(p.id)
        )
        FROM Purchase p
        JOIN Book b ON b.id = p.bookId
        WHERE b.status = com.picturebook.book.enums.BookStatus.COMPLETED
        AND b.visibility = com.picturebook.book.enums.VisibilityType.PAID
        AND p.createdAt >= :start
        AND p.createdAt < :end
        GROUP BY b.id, b.title, b.coverImageUrl, b.authorName
        ORDER BY COUNT(p.id) DESC, MAX(p.createdAt) DESC
    """)
    List<BestsellerItemResponse> findTopBestsellers(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.pages WHERE b.id = :id AND b.visibility IN :visibilities")
    Optional<Book> findByIdAndVisibilityInWithPages(@Param("id") UUID id, @Param("visibilities") Collection<VisibilityType> visibilities);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE Book b SET b.viewCount = b.viewCount + 1 WHERE b.id = :id")
    int incrementViewCount(@Param("id") UUID id);

    // 성과 조회를 위한 쿼리 (공개, 유료 작품, 완성된 작품)
    @Query("""
        SELECT b
        FROM Book b
        WHERE b.userId = :userId
        AND b.status = com.picturebook.book.enums.BookStatus.COMPLETED
        AND b.visibility IN (
            com.picturebook.book.enums.VisibilityType.PUBLIC,
            com.picturebook.book.enums.VisibilityType.PAID
        )
    """)
    Page<Book> findPerformanceBooks(
        UUID userId,
        Pageable pageable
    );

    // 책 출판 개수(completed)
    @Query("""
        SELECT COUNT(b.id)
        FROM Book b
        WHERE b.userId = :userId
        AND b.status = com.picturebook.book.enums.BookStatus.COMPLETED
    """)
    Long countPublishedBooks(UUID userId);

    // 책 작성 중 개수(draft, in_progress)
    @Query("""
        SELECT COUNT(b.id)
        FROM Book b
        WHERE b.userId = :userId
        AND b.status IN (
        com.picturebook.book.enums.BookStatus.DRAFT,
        com.picturebook.book.enums.BookStatus.IN_PROGRESS
        )
    """)
    Long countInProgressBooks(UUID userId);
}
