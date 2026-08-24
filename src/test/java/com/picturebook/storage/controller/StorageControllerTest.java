package com.picturebook.storage.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;

import com.picturebook.global.config.SecurityConfig;
import com.picturebook.global.security.CustomUserDetails;
import com.picturebook.global.security.CustomUserDetailsService;
import com.picturebook.global.security.JwtAuthenticationFilter;
import com.picturebook.global.security.JwtProvider;
import com.picturebook.global.security.oauth2.CustomOAuth2UserService;
import com.picturebook.global.security.oauth2.OAuth2SuccessHandler;
import com.picturebook.storage.port.ObjectStoragePort;
import com.picturebook.storage.port.PresignedPost;
import com.picturebook.storage.service.StorageUseCase;
import com.picturebook.user.entity.User;
import com.picturebook.user.enums.SocialProvider;

@WebMvcTest(controllers = StorageController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, StorageUseCase.class})
class StorageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ObjectStoragePort objectStoragePort;

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
    void rejectsMissingFileSize() throws Exception {
        mockMvc.perform(post("/api/storage/presigned-upload")
                        .with(user(currentUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filename\":\"image.png\",\"contentType\":\"image/png\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void rejectsZeroAndOversizedFileSize() throws Exception {
        mockMvc.perform(post("/api/storage/presigned-upload")
                        .with(user(currentUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filename\":\"image.png\",\"contentType\":\"image/png\",\"fileSize\":0}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/storage/presigned-upload")
                        .with(user(currentUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filename\":\"image.png\",\"contentType\":\"image/png\",\"fileSize\":5242881}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void acceptsValidRequestContract() throws Exception {
        org.mockito.Mockito.when(objectStoragePort.presignedUploadPost(
                        any(), eq("image/png"), eq(1L), eq(5L * 1024 * 1024), eq(java.time.Duration.ofMinutes(10))))
                .thenAnswer(invocation -> new PresignedPost(
                        "https://images.example/assets",
                        Map.of("key", invocation.getArgument(0, String.class), "Content-Type", "image/png")));
        org.mockito.Mockito.when(objectStoragePort.publicObjectUrl(any()))
                .thenAnswer(invocation -> "https://images.example/assets/" + invocation.getArgument(0, String.class));

        mockMvc.perform(post("/api/storage/presigned-upload")
                        .with(user(currentUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filename\":\"image.png\",\"contentType\":\"image/png\",\"fileSize\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.uploadUrl").value("https://images.example/assets"))
                .andExpect(jsonPath("$.data.fields.Content-Type").value("image/png"));
    }

    @Test
    void rejectsUnsupportedMimeAtHttpBoundary() throws Exception {
        mockMvc.perform(post("/api/storage/presigned-upload")
                        .with(user(currentUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filename\":\"image.svg\",\"contentType\":\"image/svg+xml\",\"fileSize\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("STORAGE_001"));
    }

    private static CustomUserDetails currentUser() {
        User user = User.builder()
                .email("user@example.test")
                .nickname("user")
                .profileImage("https://provider.example/image.png")
                .provider(SocialProvider.KAKAO)
                .providerId("provider-id")
                .build();
        ReflectionTestUtils.setField(user, "id", UUID.fromString("11111111-1111-1111-1111-111111111111"));
        return new CustomUserDetails(user, null, Map.of(), false);
    }
}
