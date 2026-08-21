package com.picturebook.like.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class BookLikeResponse {
    private UUID bookId;
    private long likeCount;
    private boolean likedByMe;
}
