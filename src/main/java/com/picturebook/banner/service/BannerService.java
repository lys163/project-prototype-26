package com.picturebook.banner.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.picturebook.banner.dto.BannerResponse;
import com.picturebook.banner.entity.Banner;
import com.picturebook.banner.repository.BannerRepository;
import com.picturebook.global.response.PageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    @Transactional(readOnly = true)
    public PageResponse<BannerResponse> getActiveBanners(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "displayOrder"));
        Page<Banner> banners = bannerRepository.findByIsActiveTrue(pageable);
        return PageResponse.from(banners.map(BannerResponse::from));
    }
}
