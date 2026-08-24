package com.picturebook.user.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.picturebook.global.response.PageResponse;
import com.picturebook.goal.dto.ReadingGoalResponse;
import com.picturebook.goal.service.UserReadingGoalService;
import com.picturebook.reading.dto.MyReadingProgressResponse;
import com.picturebook.reading.service.ReadingProgressService;
import com.picturebook.user.dto.ProfileUpdateRequest;
import com.picturebook.user.dto.ReaderDashboardResponse;
import com.picturebook.user.dto.UserLoginContext;
import com.picturebook.user.dto.UserResponseDto;
import com.picturebook.user.dto.YearlyRevenueResponse;
import com.picturebook.user.dto.AuthorSummaryResponse;
import com.picturebook.user.dto.AuthorProfileResponse;
import com.picturebook.user.dto.AuthorStatsResponse;
import com.picturebook.follow.service.AuthorFollowService;
import com.picturebook.user.dto.MonthlyRevenueResponse;
import com.picturebook.book.dto.AuthorBookResponse;
import com.picturebook.user.entity.User;
import com.picturebook.user.enums.SocialProvider;
import com.picturebook.user.repository.UserRepository;
import com.picturebook.book.service.BookService;
import com.picturebook.global.exception.CustomException;
import com.picturebook.global.exception.ErrorCode;
import com.picturebook.global.security.oauth2.OAuth2Response;
import com.picturebook.purchase.service.PurchaseService;
import com.picturebook.review.service.ReviewService;
import com.picturebook.storage.service.PublicImageUrlPolicy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final BookService bookService;
    private final PurchaseService purchaseService;
    private final ReviewService reviewService;
    private final ReadingProgressService readingProgressService;
    private final UserReadingGoalService userReadingGoalService;
    private final AuthorFollowService authorFollowService;
    private final PublicImageUrlPolicy publicImageUrlPolicy;

    @Transactional
    public UserLoginContext getOrRegisterUser(OAuth2Response response){

        SocialProvider provider = SocialProvider.valueOf(response.getProvider().toUpperCase());

        Optional<User> userOptional = userRepository.findByProviderAndProviderId(provider, response.getProviderId());

        boolean isNewUser = userOptional.isEmpty();
        User user;

        if (isNewUser){
            user = userRepository.save(User.builder()
                .email(null)
                .nickname(response.getNickname())
                .profileImage(response.getProfileImage())
                .provider(provider)
                .providerId(response.getProviderId())
                .build());
        }else{
            // 기존 유저: 카카오/네이버 원본으로 덮어쓰지 않는다.
            // 사용자가 우리 서비스에서 수정한 프로필이 로그인할 때마다 리셋되는 걸 방지.
            user = userOptional.get();
        }
        return new UserLoginContext(user, isNewUser);
    }

    @Transactional(readOnly=true)
    public UserResponseDto getUserDto(UUID userId, boolean isNewUser){
        User user = findById(userId);
        return UserResponseDto.from(user);
    }

    @Transactional(readOnly = true)
    public User findById(UUID userId){
        return userRepository.findById(userId)
            .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    // ProfileImage만 변경
    @Transactional
    public UserResponseDto updateProfileImage(UUID userId, String imageUrl){
        User user = findById(userId);
        validateProfileImage(user, imageUrl);
        user.updateProfileImage(imageUrl);

        return UserResponseDto.from(user);
    }

    // ProfileImage, info 변경
    @Transactional
    public UserResponseDto updateProfile(UUID userId, ProfileUpdateRequest request){
        User user = findById(userId);

        // 이메일이 변경되었을 경우에만
        // req.email() , user.getEmail() 이 두놈다 null일수도있으니
        if (!Objects.equals(user.getEmail(), request.email())){
            if(userRepository.existsByEmail(request.email())){
                throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
            }
        }

        validateProfileImage(user, request.profileImage());
        user.updateProfile(request.nickname(), request.email(),request.profileImage());

        return UserResponseDto.from(user);
    }

    private void validateProfileImage(User user, String requestedImageUrl) {
        if (requestedImageUrl == null || requestedImageUrl.isBlank()) {
            return;
        }
        if (Objects.equals(user.getProfileImage(), requestedImageUrl)) {
            return;
        }
        if (!publicImageUrlPolicy.isCurrentUserImageUrl(requestedImageUrl, user.getId())) {
            throw new CustomException(ErrorCode.INVALID_PROFILE_IMAGE);
        }
    }


    // 작가 수익 조회
    @Transactional(readOnly = true)
    public YearlyRevenueResponse getYearlyRevenue(
        UUID userId, 
        Integer year
    ) {

        if (year == null) {
            year = LocalDate.now().getYear();
        }

        LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime end = start.plusYears(1);

        List<MonthlyRevenueResponse> monthlyRevenueResult = purchaseService.getMonthlyRevenue(userId, start, end);

        Map<Integer, Long> revenueMap =
            monthlyRevenueResult.stream()
                .collect(Collectors.toMap(
                    MonthlyRevenueResponse::month,
                    MonthlyRevenueResponse::totalRevenue
                ));
        
        List<MonthlyRevenueResponse> monthlyRevenue = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            monthlyRevenue.add(
                new MonthlyRevenueResponse(
                month,
                revenueMap.getOrDefault(month, 0L)
                )
            );
        }

        return new YearlyRevenueResponse(
            year, monthlyRevenue
        );
    }
    // 작가 성과
    @Transactional(readOnly = true)
    public AuthorSummaryResponse getAuthorSummary(UUID userId) {
        
        // 총 수익
        Long totalRevenue = purchaseService.getTotalRevenue(userId);
        // 이번달 수익
        Long monthlyRevenue = purchaseService.getThisMonthlyRevenue(userId);
        // 총 출판 수
        Long publishedBookCount = bookService.countTotalPublishedBooks(userId);
        // 작성 중인 책 수
        Long inProgressBookCount = bookService.countInProgressBooks(userId);
        // 총 조회 수
        Long totalViewCount = userRepository.getTotalViewCount(userId);
        // 평균 책 평점
        Double averageBookRating = reviewService.getAuthorAverageRating(userId);

        // 평균 책 평점 소수점 1자리로 반올림
        averageBookRating = Math.round(averageBookRating*10)/10.0;

        return new AuthorSummaryResponse(
            totalRevenue,
            monthlyRevenue,
            publishedBookCount,
            inProgressBookCount,
            totalViewCount,
            averageBookRating
        );
    }

    // 작가 프로필 조회 (이름, 가입일, 소개)
    @Transactional(readOnly = true)
    public AuthorProfileResponse getAuthorProfile(UUID userId) {
        User user = findById(userId);
        return AuthorProfileResponse.from(user);
    }

    // 작가 통계 조회 (작품 수, 총 좋아요, 평균 평점, 팔로워 수)
    @Transactional(readOnly = true)
    public AuthorStatsResponse getAuthorStats(UUID authorId) {
        if (!userRepository.existsById(authorId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        long bookCount = bookService.getMySharedBookCount(authorId).getCount();
        long totalLikeCount = bookService.getMyReceivedLikesCount(authorId).getCount();
        long followerCount = authorFollowService.countFollowers(authorId);

        Double averageRatingObj = reviewService.getAuthorAverageRating(authorId);
        double averageRating = averageRatingObj != null ? Math.round(averageRatingObj * 10) / 10.0 : 0.0;

        return new AuthorStatsResponse(bookCount, totalLikeCount, averageRating, followerCount);
    }

    // 작가 작품 목록 조회 (공개/유료 작품)
    @Transactional(readOnly = true)
    public PageResponse<AuthorBookResponse> getAuthorBooks(UUID authorId, int page, int size) {
        if (!userRepository.existsById(authorId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        return bookService.getAuthorPublicBooks(authorId, page, size);
    }

    @Transactional(readOnly = true)
    public ReaderDashboardResponse getReaderDashboard(UUID userId, Pageable pageable) {

        long totalReadingBookCount = readingProgressService.countTotalReadingBooks(userId);
        long completedBookCount = readingProgressService.countAllCompletedBooks(userId);
        long readingBookCount = readingProgressService.countReadingBooks(userId);
        ReadingGoalResponse monthlyGoal = userReadingGoalService.getMonthlyReadingGoal(userId, null, null);
        PageResponse<MyReadingProgressResponse> recentReadingProgresses =
                readingProgressService.getMyReadingProgresses(userId, true, pageable);

        return new ReaderDashboardResponse(
                totalReadingBookCount,
                completedBookCount,
                readingBookCount,
                monthlyGoal,
                recentReadingProgresses
        );
    }
}
