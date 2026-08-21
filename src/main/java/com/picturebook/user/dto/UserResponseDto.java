package com.picturebook.user.dto;

import java.util.UUID;

import com.picturebook.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponseDto {
    private UUID userId;
    private String email;
    private String nickname;
    private String profileImage;

    public static UserResponseDto from(User user){
        return new UserResponseDto(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.getProfileImage()
        );
    }
}
