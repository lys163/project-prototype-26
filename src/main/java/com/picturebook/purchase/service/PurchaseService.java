package com.picturebook.purchase.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.picturebook.purchase.repository.PurchaseRepository;
import com.picturebook.user.dto.MonthlyRevenueResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;

    @Transactional(readOnly = true)
    public List<MonthlyRevenueResponse> getMonthlyRevenue(
        UUID userId,
        LocalDateTime start,
        LocalDateTime end
    ) {
        return purchaseRepository.findMonthlyRevenue(userId, start, end);
    }

    @Transactional(readOnly = true)
    public Long getTotalRevenue(UUID userId) {
        return purchaseRepository.findTotalRevenue(userId);
    }

    @Transactional(readOnly = true)
    public Long getThisMonthlyRevenue(UUID userId){
        LocalDate now = LocalDate.now();
        
        LocalDateTime start = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);

        return purchaseRepository.findThisMonthlyRevenue(userId, start, end);
    }
}
