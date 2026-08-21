package com.picturebook.book.dto;

import com.picturebook.book.entity.Book;
import com.picturebook.book.entity.Page;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class BookDetailResponse {

    private UUID bookId;
    private String title;
    private String description;
    private UUID authorId;
    private String authorName;
    private String coverImageUrl;

    /** 유료 책 구매 전 잠금 여부 (true면 pages는 미리보기만 포함) */
    private boolean locked;
    /** 현재 사용자의 구매 여부 */
    private boolean purchased;
    /** 책의 전체 페이지 수 (미리보기 시 "N쪽 중" 표시용) */
    private int totalPageCount;

    private List<PageResponse> pages;

    /**
     * @param locked           true면 앞 previewPageCount 페이지만 노출
     * @param purchased        현재 사용자의 구매 여부
     * @param previewPageCount 잠금 상태에서 노출할 미리보기 페이지 수
     */
    public static BookDetailResponse from(Book book, boolean locked, boolean purchased, int previewPageCount) {
        List<Page> allPages = book.getPages();

        List<PageResponse> visiblePages = (locked
                ? allPages.stream().limit(previewPageCount)
                : allPages.stream())
                .map(PageResponse::from)
                .toList();

        return BookDetailResponse.builder()
                .bookId(book.getId())
                .title(book.getTitle())
                .description(book.getDescription())
                .authorId(book.getUserId())
                .authorName(book.getAuthorName())
                .coverImageUrl(book.getCoverImageUrl())
                .locked(locked)
                .purchased(purchased)
                .totalPageCount(allPages.size())
                .pages(visiblePages)
                .build();
    }
}
