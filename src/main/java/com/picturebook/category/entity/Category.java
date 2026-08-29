package com.picturebook.category.entity;

import com.picturebook.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 책 카테고리 (마스터 데이터)
 * <p>
 * 한 책에는 한 카테고리만 매겨집니다 (Book.categoryId).
 * 운영자가 등록하고 사용자는 선택만 합니다.
 */
@Entity
@Table(name = "categories",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_categories_name", columnNames = "name")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name;

    // 카테고리 노출 순서 (낮을수록 먼저 노출)
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    // 카테고리 활성화 여부
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    // 운영자가 카테고리를 등록할 때 사용하는 생성 메서드 (추후에 관리자 서버로 분리 예정)
    public static Category create(String name, Integer displayOrder) {
        
        Category category = new Category();

        category.name = name;
        category.displayOrder = displayOrder;
        category.isActive = true;

        return category;
    }
}
