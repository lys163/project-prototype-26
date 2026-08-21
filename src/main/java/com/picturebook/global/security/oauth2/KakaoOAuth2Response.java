package com.picturebook.global.security.oauth2;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KakaoOAuth2Response implements OAuth2Response {

    private final Map<String,Object> attributes;
    public KakaoOAuth2Response(Map<String, Object> attributes){
        this.attributes=attributes;
    }
    @Override
    public String getNickname() {
        Map<String,Object> account = (Map<String,Object>) attributes.get("kakao_account");
        Map<String,Object> profile = (Map<String,Object>) account.get("profile");
        return (String) profile.get("nickname");
    }
    @Override
    public String getProfileImage() {
        Map<String,Object> account = (Map<String,Object>) attributes.get("kakao_account");
        Map<String,Object> profile = (Map<String,Object>) account.get("profile");
        return (String) profile.get("profile_image_url");
    }
    @Override
    public String getProvider() {
        return "kakao";
    }
    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

}
