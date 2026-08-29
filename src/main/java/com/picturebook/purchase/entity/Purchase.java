package com.picturebook.purchase.entity;

import com.picturebook.global.entity.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 구매 기록
 * <p>
 * 유저가 유료 책을 구매한 내역을 저장합니다.
 * price는 구매 시점의 가격, 이후 책 가격이 변경되어도 구매 기록은 유지됩니다.
 * 한 유저가 같은 책을 중복 구매할 수 없습니다.
 */
@Entity
@Table(name = "purchases",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_purchases_user_book", columnNames = {"user_id", "book_id"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Purchase extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 구매자 → users.id */
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    /** 구매한 책 → books.id */
    @Column(name = "book_id", nullable = false, columnDefinition = "uuid")
    private UUID bookId;

    /** 구매 시점 가격 */
    @Column(name = "price", nullable = false)
    private Integer price;

    @Builder
    private Purchase(UUID userId, UUID bookId, Integer price) {
        this.userId = userId;
        this.bookId = bookId;
        this.price = price;
    }
}
