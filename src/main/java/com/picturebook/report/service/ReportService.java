package com.picturebook.report.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.picturebook.book.entity.Book;
import com.picturebook.book.repository.BookRepository;
import com.picturebook.global.exception.CustomException;
import com.picturebook.global.exception.ErrorCode;
import com.picturebook.report.dto.ReportRequest;
import com.picturebook.report.dto.ReportResponse;
import com.picturebook.report.entity.Report;
import com.picturebook.report.enums.ReportReason;
import com.picturebook.report.enums.ReportStatus;
import com.picturebook.report.repository.ReportRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {
    
    private final ReportRepository reportRepository;
    private final BookRepository bookRepository;


    @Transactional
    public void createReport(UUID bookId, UUID reporterId, ReportRequest request) {

        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));
        
        // 본인 책 신고 방지
        if (book.getUserId().equals(reporterId)){
            throw new CustomException(ErrorCode.CANNOT_REPORT_OWN_BOOK);
        }

        // 중복 신고 체크
        if (reportRepository.existsByReporterIdAndBookId(reporterId, bookId)){
            throw new CustomException(ErrorCode.ALREADY_REPORTED);
        }

        Report report = Report.builder()
            .bookId(bookId)
            .reporterId(reporterId)
            .bookTitle(book.getTitle())
            .reason(request.reason())
            .detail(request.detail())
            .build();

        reportRepository.save(report);
    }
    
    @Transactional(readOnly = true)
    public Page<ReportResponse> getReportsByUser(
        UUID reporterId,
        ReportStatus status, 
        Pageable pageable
    ) {
        Page<Report> reports = reportRepository
            .findByReporterIdAndStatusOptional(
                reporterId, 
                status, 
                pageable
            );

        return reports.map(ReportResponse::from);
    }
}
