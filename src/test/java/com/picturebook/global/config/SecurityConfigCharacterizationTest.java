package com.picturebook.global.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    private static final String BOOK_ID = "11111111-1111-1111-1111-111111111101";
    private static final String USER_ID = "22222222-2222-2222-2222-222222222202";

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
    void publicBookDetailAllowsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/books/{bookId}", BOOK_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void bookMonthlySalesRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/books/{bookId}/sales/monthly", BOOK_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void bookMonthlySalesAllowsAuthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/books/{bookId}/sales/monthly", BOOK_ID)
                        .with(user("user").roles("USER")))
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
    void readingGoalGetRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/reading-goals"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void readingGoalPutRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(put("/api/reading-goals"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void readingGoalGetAllowsAuthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/reading-goals").with(user("user").roles("USER")))
                .andExpect(status().isNoContent());
    }

    @Test
    void readingGoalPutAllowsAuthenticatedRequests() throws Exception {
        mockMvc.perform(put("/api/reading-goals").with(user("user").roles("USER")))
                .andExpect(status().isNoContent());
    }

    @Test
    void readingProgressGetRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/books/{bookId}/reading-progress", BOOK_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void readingProgressPutRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(put("/api/books/{bookId}/reading-progress", BOOK_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void readingProgressCompleteRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(post("/api/books/{bookId}/reading-progress/complete", BOOK_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void readingProgressGetAllowsAuthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/books/{bookId}/reading-progress", BOOK_ID)
                        .with(user("user").roles("USER")))
                .andExpect(status().isNoContent());
    }

    @Test
    void readingProgressPutAllowsAuthenticatedRequests() throws Exception {
        mockMvc.perform(put("/api/books/{bookId}/reading-progress", BOOK_ID)
                        .with(user("user").roles("USER")))
                .andExpect(status().isNoContent());
    }

    @Test
    void readingProgressCompleteAllowsAuthenticatedRequests() throws Exception {
        mockMvc.perform(post("/api/books/{bookId}/reading-progress/complete", BOOK_ID)
                        .with(user("user").roles("USER")))
                .andExpect(status().isNoContent());
    }

    @Test
    void profileUpdateRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(patch("/api/user/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profileUpdateAllowsAuthenticatedRequests() throws Exception {
        mockMvc.perform(patch("/api/user/profile").with(user("user").roles("USER")))
                .andExpect(status().isNoContent());
    }

    @Test
    void profileImageUpdateRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(patch("/api/user/profile-image"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profileImageUpdateAllowsAuthenticatedRequests() throws Exception {
        mockMvc.perform(patch("/api/user/profile-image").with(user("user").roles("USER")))
                .andExpect(status().isNoContent());
    }

    @Test
    void publicUserProfileAllowsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/user/{userId}/profile", USER_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void categoryCreationRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(post("/api/categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void categoryCreationRejectsAuthenticatedUserRequests() throws Exception {
        mockMvc.perform(post("/api/categories").with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void categoryListAllowsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/banners",
            "/api/books",
            "/api/books/bestsellers",
            "/api/books/bestsellers/highlights",
            "/api/books/11111111-1111-1111-1111-111111111101",
            "/api/books/11111111-1111-1111-1111-111111111101/likes",
            "/api/books/11111111-1111-1111-1111-111111111101/reviews",
            "/api/categories",
            "/api/authors/22222222-2222-2222-2222-222222222202/follow",
            "/api/authors/22222222-2222-2222-2222-222222222202/stats",
            "/api/authors/22222222-2222-2222-2222-222222222202/books",
            "/api/ranking/monthly/prolific-authors",
            "/api/ranking/monthly/popular-authors",
            "/api/ranking/monthly/popular-books",
            "/api/ranking/weekly/prolific-authors",
            "/api/ranking/weekly/popular-authors",
            "/api/ranking/weekly/popular-books"
    })
    void explicitPublicEndpointsAllowAnonymousRequests(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isNoContent());
    }

    @Test
    void unmatchedEndpointRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/security-characterization/unmatched"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unmatchedEndpointRejectsAuthenticatedRequests() throws Exception {
        mockMvc.perform(get("/security-characterization/unmatched")
                        .with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }
}

@RestController
class SecurityCharacterizationEndpoints {

    @GetMapping("/api/books")
    ResponseEntity<Void> publicBookList() {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/books/{bookId}")
    ResponseEntity<Void> publicBookDetail() {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/books/{bookId}/sales/monthly")
    ResponseEntity<Void> bookMonthlySales() {
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

    @GetMapping("/api/reading-goals")
    ResponseEntity<Void> getReadingGoal() {
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/reading-goals")
    ResponseEntity<Void> putReadingGoal() {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/books/{bookId}/reading-progress")
    ResponseEntity<Void> getReadingProgress() {
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/books/{bookId}/reading-progress")
    ResponseEntity<Void> putReadingProgress() {
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/books/{bookId}/reading-progress/complete")
    ResponseEntity<Void> completeReadingProgress() {
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/user/profile")
    ResponseEntity<Void> updateProfile() {
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/user/profile-image")
    ResponseEntity<Void> updateProfileImage() {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/user/{userId}/profile")
    ResponseEntity<Void> publicUserProfile() {
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/categories")
    ResponseEntity<Void> createCategory() {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/categories")
    ResponseEntity<Void> getCategories() {
        return ResponseEntity.noContent().build();
    }

    @GetMapping({
            "/api/banners",
            "/api/books/bestsellers",
            "/api/books/bestsellers/highlights",
            "/api/books/{bookId}/likes",
            "/api/books/{bookId}/reviews",
            "/api/authors/{authorId}/follow",
            "/api/authors/{authorId}/stats",
            "/api/authors/{authorId}/books",
            "/api/ranking/monthly/prolific-authors",
            "/api/ranking/monthly/popular-authors",
            "/api/ranking/monthly/popular-books",
            "/api/ranking/weekly/prolific-authors",
            "/api/ranking/weekly/popular-authors",
            "/api/ranking/weekly/popular-books"
    })
    ResponseEntity<Void> explicitPublicEndpoints() {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/security-characterization/unmatched")
    ResponseEntity<Void> unmatchedEndpoint() {
        return ResponseEntity.noContent().build();
    }
}
