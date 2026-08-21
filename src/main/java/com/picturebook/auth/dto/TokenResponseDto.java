package com.picturebook.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponseDto {
    String accessToken;
    public static TokenResponseDto from(String accessToken) {
        return new TokenResponseDto(accessToken);
    }
}
