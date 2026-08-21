package com.picturebook.master.entity;

import com.picturebook.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 폰트 (마스터 데이터)
 * <p>
 * 도서 본문에 사용할 수 있는 폰트 목록.
 * 가독성 좋은 서체를 제공하며 사용자가 선택 가능합니다.
 */
@Entity
@Table(name = "fonts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_fonts_name", columnNames = "name")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Font extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;

    @Column(name = "font_family", nullable = false, length = 100)
    private String fontFamily;

    @Column(name = "font_url", columnDefinition = "TEXT")
    private String fontUrl;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
