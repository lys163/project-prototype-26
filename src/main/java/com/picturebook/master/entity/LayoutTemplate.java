package com.picturebook.master.entity;

import com.picturebook.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

/**
 * 레이아웃 템플릿 (마스터 데이터)
 * <p>
 * 텍스트와 이미지의 배치 방식을 정의하는 레이아웃 템플릿.
 * 책 제작 전 선택하며 페이지별 오버라이드도 가능합니다.
 */
@Entity
@Table(name = "layout_templates",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_layout_templates_name", columnNames = "name")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LayoutTemplate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "css_class", length = 100)
    private String cssClass;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "layout_config", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> layoutConfig;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
