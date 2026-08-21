package com.picturebook.user.dto;

import com.picturebook.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLoginContext {
    private final User user;
    private final boolean isNewUser;
}
