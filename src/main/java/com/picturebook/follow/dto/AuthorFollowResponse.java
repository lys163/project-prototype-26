package com.picturebook.follow.dto;

import java.util.UUID;

public record AuthorFollowResponse(
        UUID authorId,
        long followerCount,
        boolean followedByMe
) {
}
