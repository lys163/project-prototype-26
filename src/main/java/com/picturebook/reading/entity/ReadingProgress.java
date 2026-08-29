package com.picturebook.reading.entity;

import com.picturebook.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 읽기 진행도
 * <p>
 * 사용자가 책을 어디까지 읽었는지, 완독했는지를 기록합니다.
 * (user_id, book_id) 복합 유니크 제약으로 한 사용자당 책별 1건만 유지됩니다.
 * lastReadPageNumber는 마지막으로 본 페이지 번호이고,
 * completedAt이 null이 아니면 완독 상태입니다.
 */
@Entity
@Table(name = "reading_progresses",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_reading_progresses_user_book",
                        columnNames = {"user_id", "book_id"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadingProgress extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 독자 → users.id */
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    /** 읽고 있는 책 → books.id */
    @Column(name = "book_id", nullable = false, columnDefinition = "uuid")
    private UUID bookId;

    /** 마지막으로 본 페이지 번호 */
    @Column(name = "last_read_page_number", nullable = false)
    private Integer lastReadPageNumber;

    /** 완독 시각 (null이면 미완독) */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    private ReadingProgress(UUID userId, UUID bookId, Integer lastReadPageNumber) {
        this.userId = userId;
        this.bookId = bookId;
        this.lastReadPageNumber = lastReadPageNumber;
    }

    public static ReadingProgress create(UUID userId, UUID bookId, int lastReadPageNumber) {
        return new ReadingProgress(userId, bookId, lastReadPageNumber);
    }

    public void updatePage(int pageNumber) {
        this.lastReadPageNumber = pageNumber;
    }

    public void markCompleted() {
        this.completedAt = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return this.completedAt != null;
    }
}
