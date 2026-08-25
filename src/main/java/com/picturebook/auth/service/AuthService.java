package com.picturebook.auth.service;

import java.util.Collections;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.picturebook.global.exception.CustomException;
import com.picturebook.global.exception.ErrorCode;
import com.picturebook.global.security.JwtProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final DefaultRedisScript<Long> DELETE_REFRESH_TOKEN_IF_MATCHES =
        new DefaultRedisScript<>("""
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            end
            return 0
            """, Long.class);

    private final RedisTemplate<String,String> redisTemplate;
    private final JwtProvider jwtProvider;

    public void logout(String refreshToken){
        if (!StringUtils.hasText(refreshToken) || !jwtProvider.validateToken(refreshToken)) {
            return;
        }

        UUID userId;
        try {
            userId = UUID.fromString(jwtProvider.getSubject(refreshToken));
        } catch (RuntimeException exception) {
            return;
        }

        String redisKey = "RT:"+userId;
        redisTemplate.execute(
            DELETE_REFRESH_TOKEN_IF_MATCHES,
            Collections.singletonList(redisKey),
            refreshToken
        );
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
