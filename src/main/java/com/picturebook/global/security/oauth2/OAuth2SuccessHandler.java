package com.picturebook.global.security.oauth2;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.picturebook.global.security.JwtProvider;
import com.picturebook.global.security.CustomUserDetails;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler{

    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getUser().getId();
        boolean isNewUser = userDetails.isNewUser();

        String accessToken = jwtProvider.createAccessToken(userId);
        String refreshToken = jwtProvider.createRefreshToken(userId);

        // rt redis 저장
        String redisKey = "RT:"+userId.toString();
        redisTemplate.opsForValue().set(redisKey, refreshToken,Duration.ofMillis(refreshTokenExpiry));

        //rt 쿠키 저장
        ResponseCookie rtCookie = ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(true) // 개발 환경에서는 false, 운영 환경(https)에서는 true로 설정
            .path("/")
            .sameSite("Lax")
            .build();
        response.addHeader("Set-Cookie", rtCookie.toString());

        log.info("OAuth2 로그인 성공 - isNewUser: {}", isNewUser);

        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl+"/oauth/callback")
            .queryParam("accessToken", accessToken)
            .queryParam("isNewUser", isNewUser)
            .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
