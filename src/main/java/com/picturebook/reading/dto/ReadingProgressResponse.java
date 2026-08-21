package com.picturebook.reading.dto;

import com.picturebook.reading.entity.ReadingProgress;

public record ReadingProgressResponse(
    Integer lastReadPageNumber,
    boolean isCompleted
) {
    public static ReadingProgressResponse from(ReadingProgress progress) {
        return new ReadingProgressResponse(
            progress.getLastReadPageNumber(),
            progress.isCompleted()
        );
    }
}
