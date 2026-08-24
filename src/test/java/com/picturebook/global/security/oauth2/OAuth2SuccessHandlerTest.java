package com.picturebook.global.security.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import com.picturebook.global.security.CustomUserDetails;
import com.picturebook.global.security.JwtProvider;
import com.picturebook.user.entity.User;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class OAuth2SuccessHandlerTest {

    private static final String FRONTEND_URL = "http://localhost:3000";
    private static final long REFRESH_EXPIRY_MILLIS = 86_400_000L;
    private static final String REFRESH_VALUE_FIXTURE = "synthetic-refresh-value";

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private Authentication authentication;

    @Mock
    private CustomUserDetails userDetails;

    @Mock
    private User user;

    private OAuth2SuccessHandler successHandler;

    @BeforeEach
    void setUp() {
        successHandler = new OAuth2SuccessHandler(jwtProvider, redisTemplate);
        ReflectionTestUtils.setField(successHandler, "frontendUrl", FRONTEND_URL);
        ReflectionTestUtils.setField(successHandler, "refreshTokenExpiry", REFRESH_EXPIRY_MILLIS);
    }

    @Test
    void loginSuccessUsesRefreshCookieWithoutAccessTokenInRedirect(CapturedOutput output) throws Exception {
        UUID userId = UUID.randomUUID();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(userDetails.isNewUser()).thenReturn(true);
        when(jwtProvider.createRefreshToken(userId)).thenReturn(REFRESH_VALUE_FIXTURE);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        assertThat(output.getAll())
                .contains("OAuth2 로그인 성공 - isNewUser: true")
                .doesNotContain(REFRESH_VALUE_FIXTURE);

        verify(jwtProvider, never()).createAccessToken(userId);

        verify(valueOperations).set(
                "RT:" + userId,
                REFRESH_VALUE_FIXTURE,
                Duration.ofMillis(REFRESH_EXPIRY_MILLIS)
        );

        assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
                .contains("refreshToken=" + REFRESH_VALUE_FIXTURE)
                .contains("Path=/")
                .contains("Secure")
                .contains("HttpOnly")
                .contains("SameSite=Lax");

        assertThat(response.getRedirectedUrl())
                .isEqualTo(FRONTEND_URL + "/oauth/callback?isNewUser=true")
                .doesNotContain("accessToken");
    }
}
