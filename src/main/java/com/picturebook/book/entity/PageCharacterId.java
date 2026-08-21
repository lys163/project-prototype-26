package com.picturebook.book.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * PageCharacter 복합 키 (M:N 매핑 테이블용)
 */
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PageCharacterId implements Serializable {

    @Column(name = "page_id", columnDefinition = "uuid")
    private UUID pageId;

    @Column(name = "character_id", columnDefinition = "uuid")
    private UUID characterId;
}
