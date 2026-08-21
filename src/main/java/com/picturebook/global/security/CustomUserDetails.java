package com.picturebook.global.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.picturebook.user.entity.User;
import com.picturebook.global.security.oauth2.OAuth2Response;

import lombok.Getter;

@Getter
public class CustomUserDetails implements OAuth2User, UserDetails{

    private final User user;
    private final OAuth2Response oAuth2Response;
    private final Map<String, Object> attributes;
    private boolean isNewUser;

    public CustomUserDetails(User user,OAuth2Response oAuth2Response ,Map<String, Object> attributes,boolean isNewUser){
        this.user = user;
        this.oAuth2Response=oAuth2Response;
        this.attributes = attributes;
        this.isNewUser = isNewUser;
    }

    // OAuth2User 구현
    @Override
    public Map<String,Object> getAttributes(){
        return attributes;
    }
    // UserDetails 구현
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getName() {
        return user.getId().toString();
    }

    @Override
    public String getPassword(){
        return null;
    }

    @Override
    public String getUsername() {
        return user.getProviderId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부 (true: 만료 안됨)
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠김 여부 (true: 안잠김)
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 여부
    }

    @Override
    public boolean isEnabled() {
        return user.getIsActive(); // isActive 상태에 따라 활성화 여부 결정
    }
}
