package com.picturebook.user.entity;

import com.picturebook.global.entity.BaseTimeEntity;
import com.picturebook.user.enums.SocialProvider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

/**
 * 사용자 (Aggregate Root)
 * <p>
 * 서비스 사용자 계정. 카카오 또는 네이버 소셜 로그인으로만 가입 가능하며,
 * 한 유저당 하나의 소셜 계정만 연결됩니다.
 */
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_provider", columnNames = {"provider", "provider_id"})
        })
@SQLRestriction("is_active = true")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    // email nullable 허용으로 변경
    @Column(name = "email", nullable = true, length = 255, unique = true)
    private String email;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "profile_image", columnDefinition = "TEXT")
    private String profileImage;

    @Column(name = "bio", length = 500)
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private SocialProvider provider;

    @Column(name = "provider_id", nullable = false, length = 255)
    private String providerId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    // ── 생성 ──

    @Builder
    private User(String email, String nickname, String profileImage,
                SocialProvider provider, String providerId) {
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.provider = provider;
        this.providerId = providerId;
        this.isActive = true;
    }

    // ── 도메인 로직 ──

    public void updateProfileImage(String profileImage){
        this.profileImage= profileImage;
    }

    public void updateProfile(String nickname, String email, String profileImage){
        this.nickname= nickname;
        this.email=email;
        this.profileImage=profileImage;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void reactivate() {
        this.isActive = true;
    }
}
