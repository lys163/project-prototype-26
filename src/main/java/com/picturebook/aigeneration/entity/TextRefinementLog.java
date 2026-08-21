package com.picturebook.aigeneration.entity;

import com.picturebook.global.entity.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * AI 텍스트 정제(NLP) 이력
 * <p>
 * 추임새 제거·맞춤법·문체 변환 유형을 기록합니다.
 */
@Entity
@Table(name = "text_refinement_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TextRefinementLog extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 대상 페이지 → pages.id (Aggregate 간 ID 참조) */
    @Column(name = "page_id", nullable = false, columnDefinition = "uuid")
    private UUID pageId;


    @Column(name = "input_text", nullable = false, columnDefinition = "TEXT")
    private String inputText;

    @Column(name = "output_text", nullable = false, columnDefinition = "TEXT")
    private String outputText;

    @Column(name = "refinement_type", length = 50)
    private String refinementType;

    @Builder
    private TextRefinementLog(UUID pageId,
                              String inputText, String outputText,
                              String refinementType) {
        this.pageId = pageId;
        this.inputText = inputText;
        this.outputText = outputText;
        this.refinementType = refinementType;
    }

}
