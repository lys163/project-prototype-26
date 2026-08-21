package com.picturebook.global.security.oauth2;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.picturebook.global.security.CustomUserDetails;
import com.picturebook.user.dto.UserLoginContext;
import com.picturebook.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService{

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        // 카카오/네이버
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response response = switch (registrationId){
            case "kakao" -> new KakaoOAuth2Response(oAuth2User.getAttributes());
            case "naver" -> new NaverOAuth2Response(oAuth2User.getAttributes());
            default -> throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인");
        };

        UserLoginContext loginContext = userService.getOrRegisterUser(response);

        return new CustomUserDetails(loginContext.getUser(), response, oAuth2User.getAttributes(),loginContext.isNewUser());
    }


}
