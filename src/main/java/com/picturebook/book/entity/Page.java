package com.picturebook.book.entity;

import com.picturebook.book.enums.ImageSourceType;
import com.picturebook.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 페이지 (Book Aggregate 내부 엔티티)
 * <p>
 * 책의 개별 페이지. 음성(STT) 또는 텍스트로 입력한 원본과
 * AI 정제 결과, 이미지를 함께 관리합니다.
 * (book_id, page_number) 복합 유니크 제약.
 */
@Entity
@Table(name = "pages",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_pages_book_page_number",
                        columnNames = {"book_id", "page_number"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Page extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pages_book_id"))
    private Book book;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    @Column(name = "refined_text", columnDefinition = "TEXT")
    private String refinedText;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_source")
    private ImageSourceType imageSource;

    @Column(name = "image_prompt", columnDefinition = "TEXT")
    private String imagePrompt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "layout_override", columnDefinition = "jsonb")
    private Map<String, Object> layoutOverride;

    @ManyToMany
    @JoinTable(name = "page_characters",
            joinColumns = @JoinColumn(name = "page_id"),
            inverseJoinColumns = @JoinColumn(name = "character_id"))
    private Set<BookCharacter> characters = new LinkedHashSet<>();

    // ── 팩토리 메서드 (Book AR에서 호출) ──

    static Page create(Book book, int pageNumber, String rawText) {
        Page page = new Page();
        page.book = book;
        page.pageNumber = pageNumber;
        page.rawText = rawText;
        return page;
    }

    // ── 도메인 로직 ──

    public void updateRawText(String rawText) {
        this.rawText = rawText;
    }

    public void applyRefinedText(String refinedText) {
        this.refinedText = refinedText;
    }

    public void selectImage(String imageUrl, ImageSourceType source, String imagePrompt) {
        this.imageUrl = imageUrl;
        this.imageSource = source;
        this.imagePrompt = imagePrompt;
    }

    public void overrideLayout(Map<String, Object> layoutConfig) {
        this.layoutOverride = layoutConfig;
    }

    public void changePageNumber(int newPageNumber) {
        this.pageNumber = newPageNumber;
    }

    // ── 캐릭터 매핑 ──

    public void addCharacter(BookCharacter character) {
        this.characters.add(character);
    }

    public void removeCharacter(BookCharacter character) {
        this.characters.remove(character);
    }
}
