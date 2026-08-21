package com.picturebook.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.picturebook.user.entity.User;
import com.picturebook.user.enums.SocialProvider;
import java.util.List;
import java.time.LocalDateTime;


@Repository
public interface UserRepository extends JpaRepository<User, UUID>{
    Optional<User> findByProviderAndProviderId(SocialProvider provider, String providerId);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    // 총 조회 수
    @Query("""
        SELECT COALESCE(SUM(b.viewCount), 0)
        FROM Book b
        WHERE b.userId = :userId
    """)
    Long getTotalViewCount(UUID userId);
}
