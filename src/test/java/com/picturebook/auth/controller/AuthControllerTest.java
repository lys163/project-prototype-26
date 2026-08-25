package com.picturebook.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import com.picturebook.auth.service.AuthService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final String REFRESH_TOKEN = "synthetic-refresh-token";

    @Mock
    private AuthService authService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authService);
    }

    @Test
    void logoutWithRefreshTokenAlwaysExpiresCookieAndReturnsNoContent() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        ResponseEntity<Void> result = authController.logout(REFRESH_TOKEN, response);

        verify(authService).logout(REFRESH_TOKEN);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertExpiredRefreshCookie(response.getHeader(HttpHeaders.SET_COOKIE));
    }

    @Test
    void logoutWithoutRefreshTokenIsIdempotentAndExpiresCookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        ResponseEntity<Void> result = authController.logout(null, response);

        verify(authService).logout(null);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertExpiredRefreshCookie(response.getHeader(HttpHeaders.SET_COOKIE));
    }

    private void assertExpiredRefreshCookie(String setCookieHeader) {
        assertThat(setCookieHeader)
            .contains("refreshToken=")
            .contains("Path=/")
            .contains("Max-Age=0")
            .contains("Secure")
            .contains("HttpOnly")
            .contains("SameSite=Lax");
    }
}
