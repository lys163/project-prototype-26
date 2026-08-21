package com.picturebook.follow.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.picturebook.follow.dto.AuthorFollowResponse;
import com.picturebook.follow.entity.AuthorFollow;
import com.picturebook.follow.repository.AuthorFollowRepository;
import com.picturebook.global.exception.CustomException;
import com.picturebook.global.exception.ErrorCode;
import com.picturebook.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthorFollowService {

    private final AuthorFollowRepository authorFollowRepository;
    private final UserRepository userRepository;

    @Transactional
    public AuthorFollowResponse followAuthor(UUID authorId, UUID followerId) {
        validateAuthor(authorId, followerId);

        if (!authorFollowRepository.existsByFollowerIdAndAuthorId(followerId, authorId)) {
            authorFollowRepository.save(AuthorFollow.create(followerId, authorId));
        }

        return buildResponse(authorId, followerId);
    }

    @Transactional
    public AuthorFollowResponse unfollowAuthor(UUID authorId, UUID followerId) {
        validateAuthor(authorId, followerId);

        authorFollowRepository.findByFollowerIdAndAuthorId(followerId, authorId)
                .ifPresent(authorFollowRepository::delete);

        return buildResponse(authorId, followerId);
    }

    @Transactional(readOnly = true)
    public AuthorFollowResponse getAuthorFollow(UUID authorId, UUID followerId) {
        validateAuthorExists(authorId);
        return buildResponse(authorId, followerId);
    }

    @Transactional(readOnly = true)
    public long countFollowers(UUID authorId) {
        return authorFollowRepository.countByAuthorId(authorId);
    }

    private AuthorFollowResponse buildResponse(UUID authorId, UUID followerId) {
        long followerCount = authorFollowRepository.countByAuthorId(authorId);
        boolean followedByMe = followerId != null
                && authorFollowRepository.existsByFollowerIdAndAuthorId(followerId, authorId);
        return new AuthorFollowResponse(authorId, followerCount, followedByMe);
    }

    private void validateAuthor(UUID authorId, UUID followerId) {
        validateAuthorExists(authorId);
        if (authorId.equals(followerId)) {
            throw new CustomException(ErrorCode.CANNOT_FOLLOW_SELF);
        }
    }

    private void validateAuthorExists(UUID authorId) {
        if (!userRepository.existsById(authorId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }
}
