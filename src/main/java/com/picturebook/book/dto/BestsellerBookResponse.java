package com.picturebook.book.dto;

import java.util.UUID;

public record BestsellerBookResponse(
        UUID bookId,
        String title,
        String coverImageUrl,
        String authorName,
        Integer price,
        Long purchaseCount,
        Long totalRevenue,
        int rank,
        boolean liked
) {
    public BestsellerBookResponse withRankAndLiked(int rank, boolean liked) {
        return new BestsellerBookResponse(
                bookId,
                title,
                coverImageUrl,
                authorName,
                price,
                purchaseCount,
                totalRevenue,
                rank,
                liked
        );
    }
}
