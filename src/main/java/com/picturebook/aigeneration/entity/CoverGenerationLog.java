package com.picturebook.aigeneration.entity;

import com.picturebook.global.entity.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * AI 표지 생성 이력
 * <p>
 * 제목과 저자명이 포함된 표지 디자인을 생성합니다.
 * 시도 횟수 제한은 Book 엔티티의 attemptNumber로 통합 관리합니다.
 */
@Entity
@Table(name = "cover_generation_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoverGenerationLog extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 대상 책 → books.id (Aggregate 간 ID 참조) */
    @Column(name = "book_id", nullable = false, columnDefinition = "uuid")
    private UUID bookId;

    @Column(name = "title_text", nullable = false, length = 200)
    private String titleText;

    @Column(name = "author_text", length = 100)
    private String authorText;

    /** 적용된 화풍 → style_presets.id (Aggregate 간 ID 참조) */
    @Column(name = "style_preset_id")
    private Integer stylePresetId;

    @Column(name = "prompt_used", columnDefinition = "TEXT")
    private String promptUsed;

    @Column(name = "result_image_url", nullable = false, columnDefinition = "TEXT")
    private String resultImageUrl;

    @Column(name = "is_selected", nullable = false)
    private Boolean isSelected;

    @Builder
    private CoverGenerationLog(UUID bookId, String titleText, String authorText,
                               Integer stylePresetId, String promptUsed,
                               String resultImageUrl) {
        this.bookId = bookId;
        this.titleText = titleText;
        this.authorText = authorText;
        this.stylePresetId = stylePresetId;
        this.promptUsed = promptUsed;
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
