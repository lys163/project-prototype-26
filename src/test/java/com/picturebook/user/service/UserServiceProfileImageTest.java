package com.picturebook.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.picturebook.book.service.BookService;
import com.picturebook.follow.service.AuthorFollowService;
import com.picturebook.global.exception.CustomException;
import com.picturebook.global.exception.ErrorCode;
import com.picturebook.goal.service.UserReadingGoalService;
import com.picturebook.purchase.service.PurchaseService;
import com.picturebook.reading.service.ReadingProgressService;
import com.picturebook.review.service.ReviewService;
import com.picturebook.storage.service.PublicImageUrlPolicy;
import com.picturebook.user.dto.ProfileUpdateRequest;
import com.picturebook.user.entity.User;
import com.picturebook.user.enums.SocialProvider;
import com.picturebook.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceProfileImageTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String OAUTH_IMAGE = "https://provider.example/avatar.png";
    private static final String OWN_STORAGE_IMAGE = "https://images.example/assets/users/11111111-1111-1111-1111-111111111111/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa.png";

    @Mock private UserRepository userRepository;
    @Mock private BookService bookService;
    @Mock private PurchaseService purchaseService;
    @Mock private ReviewService reviewService;
    @Mock private ReadingProgressService readingProgressService;
    @Mock private UserReadingGoalService userReadingGoalService;
    @Mock private AuthorFollowService authorFollowService;
    @Mock private PublicImageUrlPolicy publicImageUrlPolicy;

    @InjectMocks private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("user@example.test")
                .nickname("user")
                .profileImage(OAUTH_IMAGE)
                .provider(SocialProvider.KAKAO)
                .providerId("provider-id")
                .build();
        ReflectionTestUtils.setField(user, "id", USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    }

    @Test
    void allowsExistingOAuthImageWhenUpdatingProfileFields() {
        userService.updateProfile(USER_ID, new ProfileUpdateRequest("changed", "user@example.test", OAUTH_IMAGE));

        assertEquals(OAUTH_IMAGE, user.getProfileImage());
        verify(publicImageUrlPolicy, never()).isCurrentUserImageUrl(any(), any());
    }

    @Test
    void rejectsNewArbitraryExternalImage() {
        when(publicImageUrlPolicy.isCurrentUserImageUrl("https://attacker.example/image.png", USER_ID)).thenReturn(false);

        CustomException exception = assertThrows(CustomException.class, () -> userService.updateProfile(
                USER_ID, new ProfileUpdateRequest("user", "user@example.test", "https://attacker.example/image.png")));

        assertEquals(ErrorCode.INVALID_PROFILE_IMAGE, exception.getErrorCode());
    }

    @Test
    void allowsOwnStorageImage() {
        when(publicImageUrlPolicy.isCurrentUserImageUrl(OWN_STORAGE_IMAGE, USER_ID)).thenReturn(true);

        userService.updateProfileImage(USER_ID, OWN_STORAGE_IMAGE);

        assertEquals(OWN_STORAGE_IMAGE, user.getProfileImage());
        verify(publicImageUrlPolicy).isCurrentUserImageUrl(eq(OWN_STORAGE_IMAGE), eq(USER_ID));
    }
}
