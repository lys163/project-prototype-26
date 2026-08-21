package com.picturebook.autosave.entity;

import com.picturebook.global.entity.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

/**
 * 자동 저장 스냅샷 (Aggregate Root)
 * <p>
 * 제작 중 자동/수동 저장 시점의 전체 책 상태를 JSON 스냅샷으로 보관합니다.
 * 복원 및 히스토리 관리에 사용됩니다.
 */
@Entity
@Table(name = "auto_save_snapshots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AutoSaveSnapshot extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 대상 책 → books.id (Aggregate 간 ID 참조) */
    @Column(name = "book_id", nullable = false, columnDefinition = "uuid")
    private UUID bookId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_data", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> snapshotData;

    @Builder
    private AutoSaveSnapshot(UUID bookId, Map<String, Object> snapshotData) {
        this.bookId = bookId;
        this.snapshotData = snapshotData;
    }

    // ── 팩토리 메서드 ──

    public static AutoSaveSnapshot capture(UUID bookId, Map<String, Object> fullBookState) {
        return AutoSaveSnapshot.builder()
                .bookId(bookId)
                .snapshotData(fullBookState)
                .build();
    }
}
