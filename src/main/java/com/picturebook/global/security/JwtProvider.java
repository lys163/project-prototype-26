package com.picturebook.global.security;



import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtProvider {

    private final SecretKey key;
    private final long accessTokenExpiry;
    private final long refreshTokenExpiry;

    public JwtProvider(
        @Value("${jwt.secret}") String secretKey,
        @Value("${jwt.access-token-expiry}") long accessTokenExpiry,
        @Value("${jwt.refresh-token-expiry}") long refreshTokenExpiry
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    public String createAccessToken(UUID userId){
        return createToken(userId, accessTokenExpiry);
    }

    public String createRefreshToken(UUID userId){
        return createToken(userId, refreshTokenExpiry);
    }

    private String createToken(UUID userId, Long expiry){
        Date now = new Date();
        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(new Date(now.getTime()+expiry))
            .signWith(key)
            .compact();
    }
    public boolean validateToken(String token){
        try{
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e){
            log.info("잘못된 jwt 토큰: {}", e.getMessage());
            return false;
        }
    }

    public String getSubject(String token){
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }
}
