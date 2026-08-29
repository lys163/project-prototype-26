package com.picturebook.master.entity;

import com.picturebook.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 화풍 프리셋 (마스터 데이터)
 * <p>
 * AI 이미지 생성 시 적용할 화풍 프리셋 (10종 이상).
 * 동화책, 유화, 수채화, 3D 렌더링 등 다양한 스타일을 제공합니다.
 */
@Entity
@Table(name = "style_presets",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_style_presets_name", columnNames = "name")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StylePreset extends BaseTimeEntity {

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

    @Column(name = "prompt_prefix", nullable = false, columnDefinition = "TEXT")
    private String promptPrefix;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
