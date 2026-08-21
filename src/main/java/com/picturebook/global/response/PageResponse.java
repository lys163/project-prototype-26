package com.picturebook.global.response;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PageResponse<T> {

    private List<T> items;
    private int page;
    private int size;
    private long totalCount;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean first;
    private boolean last;

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .items(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalCount(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
