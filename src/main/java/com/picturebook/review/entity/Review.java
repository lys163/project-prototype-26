package com.picturebook.review.entity;

import com.picturebook.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "reviews",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_reviews_user_book",
                columnNames = {"user_id", "book_id"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "book_id", nullable = false)
    private UUID bookId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "content", length = 500)
    private String content;

    private Review(UUID userId, UUID bookId, Integer rating, String content) {
        this.userId = userId;
        this.bookId = bookId;
        this.rating = rating;
        this.content = content;
    }

    public static Review create(UUID userId, UUID bookId, Integer rating, String content) {
        return new Review(userId, bookId, rating, content);
    }

    public void update(Integer rating, String content) {
        this.rating = rating;
        this.content = content;
    }
}
