package com.picturebook.aigeneration.entity;

import com.picturebook.aigeneration.enums.SourceType;
import com.picturebook.global.entity.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * AI 이미지 생성 이력
 * <p>
 * 사용자가 최종 선택한 이미지를 is_selected로 표시합니다.
 */
@Entity
@Table(name = "image_generation_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageGenerationLog extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 대상 페이지 → pages.id (Aggregate 간 ID 참조) */
    @Column(name = "page_id", nullable = false, columnDefinition = "uuid")
    private UUID pageId;


    @Column(name = "prompt_used", nullable = false, columnDefinition = "TEXT")
    private String promptUsed;

    /** 적용된 화풍 → style_presets.id (Aggregate 간 ID 참조) */
    @Column(name = "style_preset_id")
    private Integer stylePresetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @Column(name = "source_image_url", columnDefinition = "TEXT")
    private String sourceImageUrl;

    @Column(name = "result_image_url", nullable = false, columnDefinition = "TEXT")
    private String resultImageUrl;

    @Column(name = "is_selected", nullable = false)
    private Boolean isSelected;

    @Builder
    private ImageGenerationLog(UUID pageId,
                               String promptUsed, Integer stylePresetId,
                               SourceType sourceType, String sourceImageUrl,
                               String resultImageUrl) {
        this.pageId = pageId;
        this.promptUsed = promptUsed;
        this.stylePresetId = stylePresetId;
        this.sourceType = sourceType;
        this.sourceImageUrl = sourceImageUrl;
        this.resultImageUrl = resultImageUrl;
        this.isSelected = false;
    }

    // ── 도메인 로직 ──

    public void select() {
        this.isSelected = true;
    }

    public void deselect() {
        this.isSelected = false;
    }

}
