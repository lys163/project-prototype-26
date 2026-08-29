package com.picturebook.book.entity;

import com.picturebook.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 캐릭터 (Book Aggregate 내부 엔티티)
 * <p>
 * 캐릭터 일관성(Consistency) 유지를 위한 외형 정보 저장.
 * 프롬프트 조각이 이미지 생성 시 자동으로 삽입됩니다.
 * <p>
 * java.lang.Character와 이름 충돌 방지를 위해 BookCharacter로 명명.
 * 테이블명은 "characters"로 매핑됩니다.
 */
@Entity
@Table(name = "characters")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookCharacter extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_characters_book"))
    private Book book;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "reference_image_url", columnDefinition = "TEXT")
    private String referenceImageUrl;

    @Column(name = "prompt_fragment", nullable = false, columnDefinition = "TEXT")
    private String promptFragment;

    // ── 팩토리 메서드 (Book AR에서 호출) ──

    static BookCharacter create(Book book, String name, String description, String promptFragment) {
        BookCharacter character = new BookCharacter();
        character.book = book;
        character.name = name;
        character.description = description;
        character.promptFragment = promptFragment;
        return character;
    }

    // ── 도메인 로직 ──

    public void updateAppearance(String name, String description,
                                 String referenceImageUrl, String promptFragment) {
        this.name = name;
        this.description = description;
        this.referenceImageUrl = referenceImageUrl;
        this.promptFragment = promptFragment;
    }
}
