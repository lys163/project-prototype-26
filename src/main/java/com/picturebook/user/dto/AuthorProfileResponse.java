package com.picturebook.user.dto;

import java.time.LocalDateTime;

import com.picturebook.user.entity.User;

public record AuthorProfileResponse(
    // 작가 이름
    String nickname,
    // 작가 프로필 이미지
    String profileImage,
    // 가입일
    LocalDateTime joinedAt,
    // 작가 소개
    String bio
) {
    public static AuthorProfileResponse from(User user) {
        return new AuthorProfileResponse(
            user.getNickname(),
            user.getProfileImage(),
            user.getCreatedAt(),
            user.getBio()
        );
    }
}
