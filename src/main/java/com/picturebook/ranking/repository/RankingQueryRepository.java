package com.picturebook.ranking.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.picturebook.book.enums.VisibilityType;
import com.picturebook.ranking.dto.PopularAuthorResponse;
import com.picturebook.ranking.dto.PopularBookResponse;
import com.picturebook.ranking.dto.ProlificAuthorResponse;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RankingQueryRepository {

    private final EntityManager em;
    
    // 가장 많은 책 출간 작가 (public 책만, 출간일 기준)
    public List<ProlificAuthorResponse> findTopAuthorsByBookCount(LocalDateTime startDate, LocalDateTime endDate, VisibilityType visibility, int limit) {
        String jpql = """
SELECT new com.picturebook.ranking.dto.ProlificAuthorResponse(
	u.id, u.nickname, u.profileImage, Count(b), 0)
FROM User u 
JOIN Book b ON u.id = b.userId
WHERE b.publishedAt >= :startDate AND b.publishedAt < :endDate
AND b.visibility = :visibility
GROUP BY u.id, u.nickname, u.profileImage 
ORDER BY Count(b) DESC, MAX(b.publishedAt) DESC
""";

        return em.createQuery(jpql, ProlificAuthorResponse.class)
            .setParameter("startDate", startDate)
            .setParameter("endDate", endDate)
            .setParameter("visibility", visibility)
            .setMaxResults(limit)
            .getResultList();
    }

    // 가장 많은 좋아요를 받은 작가 (public 책만 좋아요 생성일 기준)
    public List<PopularAuthorResponse> findTopAuthorsByTotalLikes(LocalDateTime startDate, LocalDateTime endDate, VisibilityType visibility, int limit){
        String jpql = """
SELECT new com.picturebook.ranking.dto.PopularAuthorResponse(
	u.id, u.nickname, u.profileImage, COUNT(l.id), 0) 
FROM User u  
JOIN Book b ON u.id = b.userId
JOIN BookLike l ON b.id = l.bookId
WHERE l.createdAt >= :startDate AND l.createdAt < :endDate
AND b.visibility = :visibility
GROUP BY u.id, u.nickname, u.profileImage
ORDER BY COUNT(l.id) DESC, MAX(l.createdAt) DESC
""";

        return em.createQuery(jpql,PopularAuthorResponse.class)
            .setParameter("startDate", startDate)
            .setParameter("endDate", endDate)
            .setParameter("visibility", visibility)
            .setMaxResults(limit)
            .getResultList();
    }

    // 가장 많은 좋아요를 받은 책 (public 책만, 좋아요 생성일 기준)
    public List<PopularBookResponse> findTopBooksByLikes(LocalDateTime startDate,LocalDateTime endDate, VisibilityType visibility, int limit) {
    String jpql = """
SELECT new com.picturebook.ranking.dto.PopularBookResponse(
	b.id, b.title, b.coverImageUrl, u.nickname, COUNT(l.id), 0)
FROM Book b  
JOIN User u ON b.userId = u.id
JOIN BookLike l ON b.id = l.bookId
WHERE l.createdAt >= :startDate AND l.createdAt < :endDate
AND b.visibility = :visibility
GROUP BY b.id, b.title, b.coverImageUrl, u.nickname
ORDER BY COUNT(l.id) DESC, MAX(l.createdAt) DESC
""";

    return em.createQuery(jpql, PopularBookResponse.class)
            .setParameter("startDate", startDate)
            .setParameter("endDate", endDate)
            .setParameter("visibility", visibility)
            .setMaxResults(limit)
            .getResultList();
    }
    
}
