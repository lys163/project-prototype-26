package com.picturebook.follow.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import com.picturebook.follow.entity.AuthorFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import com.picturebook.follow.entity.AuthorFollow;

public interface AuthorFollowRepository extends JpaRepository<AuthorFollow, UUID> {

    boolean existsByFollowerIdAndAuthorId(UUID followerId, UUID authorId);

    Optional<AuthorFollow> findByFollowerIdAndAuthorId(UUID followerId, UUID authorId);

    long countByAuthorId(UUID authorId);
}
