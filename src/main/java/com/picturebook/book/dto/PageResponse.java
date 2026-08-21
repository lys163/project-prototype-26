package com.picturebook.book.dto;

import com.picturebook.book.entity.Page;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PageResponse {

    private int pageNumber;
    private String content;
    private String imageUrl;


    public static PageResponse from(Page page) {
        return PageResponse.builder()
                .pageNumber(page.getPageNumber())
                .content(page.getRawText())
                .imageUrl(page.getImageUrl())
                .build();
    }
}
