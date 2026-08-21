package com.picturebook.purchase.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.picturebook.book.dto.MonthlySalesResponse;
import com.picturebook.purchase.entity.Purchase;
import com.picturebook.user.dto.MonthlyRevenueResponse;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {

    boolean existsByUserIdAndBookId(UUID userId, UUID bookId);

    @Query("""
        SELECT new com.picturebook.user.dto.MonthlyRevenueResponse(
            MONTH(p.createdAt),
            SUM(p.price)
        )
        FROM Purchase p
        JOIN Book b ON b.id = p.bookId
        WHERE b.userId = :userId
          AND p.createdAt >= :start
          AND p.createdAt < :end
        GROUP BY MONTH(p.createdAt)
        ORDER BY MONTH(p.createdAt)
    """)
    List<MonthlyRevenueResponse> findMonthlyRevenue(
            UUID userId,
            LocalDateTime start,
            LocalDateTime end
    );

    // 작품별 수익 조회
    @Query("""
        SELECT p.bookId, SUM(p.price)
        FROM Purchase p
        WHERE p.bookId IN :bookIds
        GROUP BY p.bookId
    """)
    List<Object[]> sumRevenue(List<UUID> bookIds);

    // 특정 작품의 월별 판매 건수 조회 (본인 작품만, 판매가 있는 달만 반환)
    @Query("""
        SELECT new com.picturebook.book.dto.MonthlySalesResponse(
            MONTH(p.createdAt),
            COUNT(p)
        )
        FROM Purchase p
        JOIN Book b ON b.id = p.bookId
        WHERE b.userId = :userId
          AND b.id = :bookId
          AND p.createdAt >= :start
          AND p.createdAt < :end
        GROUP BY MONTH(p.createdAt)
        ORDER BY MONTH(p.createdAt)
    """)
    List<MonthlySalesResponse> findMonthlySalesCount(
            UUID userId,
            UUID bookId,
            LocalDateTime start,
            LocalDateTime end
    );

    // 총 수익 조회
    @Query("""
        SELECT COALESCE(SUM(p.price), 0) 
        FROM Purchase p 
        JOIN Book b ON b.id = p.bookId 
        WHERE b.userId = :userId 
        AND b.status = 
        com.picturebook.book.enums.BookStatus.COMPLETED 
        AND b.visibility = 
        com.picturebook.book.enums.VisibilityType.PAID 
    """) 
    Long findTotalRevenue(UUID userId);

    // 이번달 수익 조회
    @Query("""
        SELECT COALESCE(SUM(p.price), 0)
        FROM Purchase p
        JOIN Book b ON b.id = p.bookId
        WHERE b.userId = :userId
        AND b.status = com.picturebook.book.enums.BookStatus.COMPLETED
        AND b.visibility = com.picturebook.book.enums.VisibilityType.PAID
        AND p.createdAt >= :start
        AND p.createdAt < :end
    """)
    Long findThisMonthlyRevenue(UUID userId, LocalDateTime start, LocalDateTime end);
}
