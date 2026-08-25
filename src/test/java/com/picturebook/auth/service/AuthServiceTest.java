package com.picturebook.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import com.picturebook.global.security.JwtProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"rawtypes", "unchecked"})
class AuthServiceTest {

    private static final String REFRESH_TOKEN = "synthetic-refresh-token";

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private JwtProvider jwtProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(redisTemplate, jwtProvider);
    }

    @Test
    void logoutWithoutRefreshTokenIsIdempotent() {
        authService.logout(null);

        verifyNoInteractions(jwtProvider, redisTemplate);
    }

    @Test
    void logoutWithExpiredRefreshTokenDoesNotTouchRedis() {
        when(jwtProvider.validateToken(REFRESH_TOKEN)).thenReturn(false);

        authService.logout(REFRESH_TOKEN);

        verify(jwtProvider, never()).getSubject(anyString());
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void logoutWithMalformedRefreshTokenDoesNotTouchRedis() {
        String malformedToken = "malformed-refresh-token";
        when(jwtProvider.validateToken(malformedToken)).thenReturn(false);

        authService.logout(malformedToken);

        verify(jwtProvider, never()).getSubject(anyString());
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void logoutWithInvalidSubjectDoesNotTouchRedis() {
        when(jwtProvider.validateToken(REFRESH_TOKEN)).thenReturn(true);
        when(jwtProvider.getSubject(REFRESH_TOKEN)).thenReturn("not-a-uuid");

        authService.logout(REFRESH_TOKEN);

        verifyNoInteractions(redisTemplate);
    }

    @Test
    void logoutDeletesOnlyWhenStoredRefreshTokenExactlyMatches() {
        UUID userId = UUID.randomUUID();
        String redisKey = "RT:" + userId;
        when(jwtProvider.validateToken(REFRESH_TOKEN)).thenReturn(true);
        when(jwtProvider.getSubject(REFRESH_TOKEN)).thenReturn(userId.toString());
        when(redisTemplate.execute(any(RedisScript.class), eq(Collections.singletonList(redisKey)), eq(REFRESH_TOKEN)))
            .thenReturn(1L);

        authService.logout(REFRESH_TOKEN);

        ArgumentCaptor<RedisScript<Long>> scriptCaptor = ArgumentCaptor.forClass((Class) RedisScript.class);
        verify(redisTemplate).execute(
            scriptCaptor.capture(),
            eq(Collections.singletonList(redisKey)),
            eq(REFRESH_TOKEN)
        );
        verify(redisTemplate, never()).delete(anyString());
        assertThat(scriptCaptor.getValue().getScriptAsString())
            .contains("redis.call('get', KEYS[1]) == ARGV[1]")
            .contains("redis.call('del', KEYS[1])");
    }

    @Test
    void logoutWithMissingStoredRefreshTokenDoesNotDeleteAnotherToken() {
        UUID userId = UUID.randomUUID();
        String redisKey = "RT:" + userId;
        when(jwtProvider.validateToken(REFRESH_TOKEN)).thenReturn(true);
        when(jwtProvider.getSubject(REFRESH_TOKEN)).thenReturn(userId.toString());
        when(redisTemplate.execute(any(RedisScript.class), eq(Collections.singletonList(redisKey)), eq(REFRESH_TOKEN)))
            .thenReturn(0L);

        authService.logout(REFRESH_TOKEN);

        verify(redisTemplate).execute(any(RedisScript.class), eq(Collections.singletonList(redisKey)), eq(REFRESH_TOKEN));
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void logoutWithMismatchedStoredRefreshTokenDoesNotDeleteCurrentToken() {
        UUID userId = UUID.randomUUID();
        String redisKey = "RT:" + userId;
        when(jwtProvider.validateToken(REFRESH_TOKEN)).thenReturn(true);
        when(jwtProvider.getSubject(REFRESH_TOKEN)).thenReturn(userId.toString());
        when(redisTemplate.execute(any(RedisScript.class), eq(Collections.singletonList(redisKey)), eq(REFRESH_TOKEN)))
            .thenReturn(0L);

        authService.logout(REFRESH_TOKEN);

        verify(redisTemplate).execute(any(RedisScript.class), eq(Collections.singletonList(redisKey)), eq(REFRESH_TOKEN));
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void repeatedLogoutRemainsIdempotent() {
        UUID userId = UUID.randomUUID();
        String redisKey = "RT:" + userId;
        when(jwtProvider.validateToken(REFRESH_TOKEN)).thenReturn(true);
        when(jwtProvider.getSubject(REFRESH_TOKEN)).thenReturn(userId.toString());
        when(redisTemplate.execute(any(RedisScript.class), eq(Collections.singletonList(redisKey)), eq(REFRESH_TOKEN)))
            .thenReturn(1L, 0L);

        authService.logout(REFRESH_TOKEN);
        authService.logout(REFRESH_TOKEN);

        verify(redisTemplate, org.mockito.Mockito.times(2)).execute(
            any(RedisScript.class),
            eq(Collections.singletonList(redisKey)),
            eq(REFRESH_TOKEN)
        );
    }
}
