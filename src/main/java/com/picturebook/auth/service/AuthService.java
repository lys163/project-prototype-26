package com.picturebook.auth.service;

import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.picturebook.global.exception.CustomException;
import com.picturebook.global.exception.ErrorCode;
import com.picturebook.global.security.JwtProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RedisTemplate<String,String> redisTemplate;
    private final JwtProvider jwtProvider;

    public void logout(UUID userId){
        String redisKey = "RT:"+userId.toString();

        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))){
            redisTemplate.delete(redisKey);
        }
    }

    public String refreshAccessToken(String refreshToken){

        if (!jwtProvider.validateToken(refreshToken)){
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        UUID userId = UUID.fromString(jwtProvider.getSubject(refreshToken));

        String redisKey = "RT:"+userId.toString();

        String savedRt = redisTemplate.opsForValue().get(redisKey);

        if (savedRt==null){
            throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }
        if (!savedRt.equals(refreshToken)){
            throw new CustomException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }
        return jwtProvider.createAccessToken(userId);
    }
}