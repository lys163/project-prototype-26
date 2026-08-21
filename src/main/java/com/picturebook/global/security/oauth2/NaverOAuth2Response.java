package com.picturebook.global.security.oauth2;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NaverOAuth2Response implements OAuth2Response {

    private final Map<String, Object> attributes;

    public NaverOAuth2Response(Map<String,Object> attributes){
        this.attributes=(Map<String,Object>) attributes.get("response");
    }

    @Override
    public String getNickname() {
        // 별명
        // return (String)attributes.get("nickname");
        // 이름
        return (String)attributes.get("name");
    }

    @Override
    public String getProfileImage() {
        return (String)attributes.get("profile_image");
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderId() {
        return (String)attributes.get("id");
    }

}
