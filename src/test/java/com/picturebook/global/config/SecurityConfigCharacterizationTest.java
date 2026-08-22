package com.picturebook.global.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.picturebook.global.security.CustomUserDetailsService;
import com.picturebook.global.security.JwtAuthenticationFilter;
import com.picturebook.global.security.JwtProvider;
import com.picturebook.global.security.oauth2.CustomOAuth2UserService;
import com.picturebook.global.security.oauth2.OAuth2SuccessHandler;

@ActiveProfiles("test")
@WebMvcTest(controllers = SecurityCharacterizationEndpoints.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class SecurityConfigCharacterizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    void publicBookListAllowsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isNoContent());
    }

    @Test
    void myBookListRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/books/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void myBookListAllowsAuthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/books/me").with(user("user").roles("USER")))
                .andExpect(status().isNoContent());
    }

    @Test
    void storageUploadRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(post("/api/storage/presigned-upload"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void readingGoalCurrentlyAllowsAnonymousRequests() throws Exception {
        mockMvc.perform(put("/api/reading-goals"))
                .andExpect(status().isNoContent());
    }
}

@RestController
class SecurityCharacterizationEndpoints {

    @GetMapping("/api/books")
    ResponseEntity<Void> publicBookList() {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/books/me")
    ResponseEntity<Void> myBookList() {
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/storage/presigned-upload")
    ResponseEntity<Void> storageUpload() {
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/reading-goals")
    ResponseEntity<Void> readingGoal() {
        return ResponseEntity.noContent().build();
    }
}
