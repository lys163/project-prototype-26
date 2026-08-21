package com.picturebook.global.security;

import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.picturebook.user.entity.User;
import com.picturebook.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        try{
            User user = userService.findById(UUID.fromString(userId));

            return new CustomUserDetails(user, null, null, false);
        }catch (Exception e){
            throw new UsernameNotFoundException("인증 과정에서 유저를 찾을 수 없습니다.");
        }
    }
}
