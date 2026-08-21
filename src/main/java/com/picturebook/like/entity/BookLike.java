package com.picturebook.like.entity;

import com.picturebook.global.entity.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(
        name = "book_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_book_likes_user_book",
                columnNames = {"user_id", "book_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookLike extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "book_id", nullable = false)
    private UUID bookId;

    private BookLike(UUID userId, UUID bookId) {
        this.userId = userId;
        this.bookId = bookId;
    }

    public static BookLike create(UUID userId, UUID bookId) {
        return new BookLike(userId, bookId);
    }
}
