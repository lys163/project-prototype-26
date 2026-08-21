package com.picturebook.banner.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.picturebook.banner.entity.Banner;

public interface BannerRepository extends JpaRepository<Banner, UUID> {

    Page<Banner> findByIsActiveTrue(Pageable pageable);
}
