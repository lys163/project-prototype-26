package com.picturebook.book.entity;

import com.picturebook.book.enums.BookStatus;
import com.picturebook.book.enums.VisibilityType;
import com.picturebook.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 책 (Aggregate Root)
 * <p>
 * 사용자가 제작하는 그림책 단위. 장르는 '그림책'으로 고정되어 있으며,
 * 화풍·레이아웃·폰트를 마스터 테이블에서 참조합니다.
 * <p>
 * Page, BookCharacter를 하위 엔티티로 포함하는 Aggregate Root입니다.
 */
@Entity
@Table(name = "books",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_books_share_link_token", columnNames = "share_link_token")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 제작자 → users.id (Aggregate 간 ID 참조) */
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 카테고리 → categories.id (Aggregate 간 ID 참조) */
    @Column(name = "category_id")
    private Integer categoryId;

    /** 선택한 화풍 → style_presets.id (Aggregate 간 ID 참조) */
    @Column(name = "style_preset_id")
    private Integer stylePresetId;

    /** 선택한 레이아웃 → layout_templates.id */
    @Column(name = "layout_template_id")
    private Integer layoutTemplateId;

    /** 선택한 폰트 → fonts.id */
    @Column(name = "font_id")
    private Integer fontId;

    /** 가격 (null 또는 0이면 무료, 양수면 유료) */
    @Column(name = "price")
    private Integer price;

    @Column(name = "cover_image_url", columnDefinition = "TEXT")
    private String coverImageUrl;

    @Column(name = "author_name", length = 100)
    private String authorName;

    @Column(name = "target_page_count")
    private Integer targetPageCount;

    @Column(name = "font_size")
    private Integer fontSize;

    @Column(name = "font_color", length = 7)
    private String fontColor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private VisibilityType visibility;

    @Column(name = "share_link_token", length = 64, unique = true)
    private String shareLinkToken;

    /** AI 생성 시도 횟수 (텍스트 정제 + 이미지 생성 + 표지 생성 통합, 최대 5회) */
    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    private static final int MAX_ATTEMPT_NUMBER = 5;

    @Column(name = "last_saved_at")
    private LocalDateTime lastSavedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    // ── 하위 엔티티 (Aggregate 내부) ──

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("pageNumber ASC")
    private List<Page> pages = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BookCharacter> characters = new HashSet<>();

    // ── 생성 ──

    @Builder
    private Book(UUID userId, String title, String authorName,
                 Integer stylePresetId, Integer layoutTemplateId, Integer fontId,
                 Integer targetPageCount) {
        this.userId = userId;
        this.title = (title != null) ? title : "제목 없음";
        this.authorName = authorName;
        this.stylePresetId = stylePresetId;
        this.layoutTemplateId = layoutTemplateId;
        this.fontId = fontId;
        this.targetPageCount = targetPageCount;
        this.fontSize = 16;
        this.fontColor = "#000000";
        this.attemptNumber = 0;
        this.viewCount = 0L;
        this.status = BookStatus.DRAFT;
        this.visibility = VisibilityType.PRIVATE;
    }

    // ── 상태 전이 ──

    public void startProgress() {
        validateStatusTransition(BookStatus.DRAFT, BookStatus.IN_PROGRESS);
        this.status = BookStatus.IN_PROGRESS;
    }

    public void complete() {
        validateStatusTransition(BookStatus.IN_PROGRESS, BookStatus.COMPLETED);
        this.status = BookStatus.COMPLETED;
    }

    private void validateStatusTransition(BookStatus expected, BookStatus next) {
        if (this.status != expected) {
            throw new IllegalStateException(
                    String.format("책 상태를 %s → %s로 변경할 수 없습니다. 현재 상태: %s",
                            expected, next, this.status));
        }
    }

    // ── 공개 설정 ──

    public void publish(String shareLinkToken) {
        this.visibility = VisibilityType.PUBLIC;
        this.shareLinkToken = shareLinkToken;
        this.publishedAt = LocalDateTime.now();
    }

    public void publishAsPaid(int price, String shareLinkToken) {
        if (price <= 0) {
            throw new IllegalArgumentException("유료 출판 가격은 0보다 커야 합니다.");
        }
        this.visibility = VisibilityType.PAID;
        this.price = price;
        if (this.shareLinkToken == null) {
            this.shareLinkToken = shareLinkToken;
        }
        if (this.publishedAt == null) {
            this.publishedAt = LocalDateTime.now();
        }
    }

    public void shareByLink(String shareLinkToken) {
        this.visibility = VisibilityType.LINK_SHARED;
        this.shareLinkToken = shareLinkToken;
    }

    public void makePrivate() {
        this.visibility = VisibilityType.PRIVATE;
        this.shareLinkToken = null;
    }

    // ── 책 정보 수정 ──

    public void updateBookInfo(String title, String description, String authorName) {
        this.title = title;
        this.description = description;
        this.authorName = authorName;
    }

    public void updateStyle(Integer stylePresetId, Integer layoutTemplateId,
                            Integer fontId, Integer fontSize, String fontColor) {
        this.stylePresetId = stylePresetId;
        this.layoutTemplateId = layoutTemplateId;
        this.fontId = fontId;
        this.fontSize = fontSize;
        this.fontColor = fontColor;
    }

    public void updateCoverImage(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public void updateCategory(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public void markSaved() {
        this.lastSavedAt = LocalDateTime.now();
    }

    // ── AI 생성 시도 횟수 관리 ──

    public void incrementAttempt() {
        if (this.attemptNumber >= MAX_ATTEMPT_NUMBER) {
            throw new IllegalStateException(
                    String.format("AI 생성 최대 시도 횟수(%d회)를 초과했습니다.", MAX_ATTEMPT_NUMBER));
        }
        this.attemptNumber++;
    }

    public boolean canAttempt() {
        return this.attemptNumber < MAX_ATTEMPT_NUMBER;
    }

    public int getRemainingAttempts() {
        return MAX_ATTEMPT_NUMBER - this.attemptNumber;
    }

    // ── 연관관계 편의 메서드 ──

    public Page addPage(int pageNumber, String rawText) {
        Page page = Page.create(this, pageNumber, rawText);
        this.pages.add(page);
        return page;
    }

    public void removePage(Page page) {
        this.pages.remove(page);
    }

    public BookCharacter addCharacter(String name, String description, String promptFragment) {
        BookCharacter character = BookCharacter.create(this, name, description, promptFragment);
        this.characters.add(character);
        return character;
    }

    public void removeCharacter(BookCharacter character) {
        this.characters.remove(character);
    }
}
