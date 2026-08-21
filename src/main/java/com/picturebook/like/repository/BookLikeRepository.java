package com.picturebook.like.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.picturebook.book.enums.VisibilityType;
import com.picturebook.like.dto.LikedBookResponse;
import com.picturebook.like.entity.BookLike;

public interface BookLikeRepository extends JpaRepository<BookLike, UUID> {

    @Query("SELECT bl.bookId FROM BookLike bl WHERE bl.userId = :userId AND bl.bookId IN :bookIds")
    Set<UUID> findBookIdsByUserIdAndBookIdIn(@Param("userId") UUID userId, @Param("bookIds") List<UUID> bookIds);

    boolean existsByUserIdAndBookId(UUID userId, UUID bookId);

    Optional<BookLike> findByUserIdAndBookId(UUID userId, UUID bookId);

    long countByBookId(UUID bookId);

    @Query(
            value = """
                    SELECT new com.picturebook.like.dto.LikedBookResponse(
                        b.id,
                        b.title,
                        b.coverImageUrl,
                        b.authorName,
                        bl.createdAt,
                        true
                    )
                    FROM BookLike bl
                    JOIN Book b ON b.id = bl.bookId
                    WHERE bl.userId = :userId
                      AND b.visibility IN :visibilities
                    ORDER BY bl.createdAt DESC
                    """,
            countQuery = """
                    SELECT COUNT(bl)
                    FROM BookLike bl
                    JOIN Book b ON b.id = bl.bookId
                    WHERE bl.userId = :userId
                      AND b.visibility IN :visibilities
                    """
    )
    Page<LikedBookResponse> findLikedBooksByUserIdAndVisibilityIn(
            @Param("userId") UUID userId,
            @Param("visibilities") Collection<VisibilityType> visibilities,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(bl)
            FROM BookLike bl
            JOIN Book b ON b.id = bl.bookId
            WHERE b.userId = :userId
              AND b.visibility IN :visibilities
            """)
    long countReceivedLikesByUserIdAndVisibilityIn(
        @Param("userId") UUID userId,
        @Param("visibilities") Collection<com.picturebook.book.enums.VisibilityType> visibilities
    );

    // 좋아요 수 조회 쿼리 
    @Query("""
        SELECT bl.bookId, COUNT(bl.id)
        FROM BookLike bl
        WHERE bl.bookId IN :bookIds
        GROUP BY bl.bookId
    """)
    List<Object[]> countLikes(List<UUID> bookIds);
}
