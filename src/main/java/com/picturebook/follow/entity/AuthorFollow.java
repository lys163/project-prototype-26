package com.picturebook.follow.entity;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import com.picturebook.global.entity.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 작가 팔로우
 * <p>
 * 한 사용자가 다른 사용자(작가)를 팔로우한 관계를 표현합니다.
 * (follower_id, author_id) 복합 유니크 제약으로 동일 팔로우 중복을 방지합니다.
 * 해제 시 행을 삭제합니다.
 */
@Entity
@Table(name = "author_follows",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_author_follows_follower_author",
                        columnNames = {"follower_id", "author_id"})
        },
        indexes = {
                @Index(name = "idx_author_follows_author", columnList = "author_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthorFollow extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "follower_id", nullable = false, columnDefinition = "uuid")
    private UUID followerId;

    @Column(name = "author_id", nullable = false, columnDefinition = "uuid")
    private UUID authorId;

    private AuthorFollow(UUID followerId, UUID authorId) {
        this.followerId = followerId;
        this.authorId = authorId;
    }

    public static AuthorFollow create(UUID followerId, UUID authorId) {
        return new AuthorFollow(followerId, authorId);
    }

}
