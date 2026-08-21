package com.picturebook.report.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.picturebook.report.entity.Report;
import com.picturebook.report.enums.ReportStatus;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID>{
    boolean existsByReporterIdAndBookId(UUID reporterId, UUID bookId);

    @Query("SELECT r FROM Report r WHERE r.reporterId = :reporterId AND (:status IS NULL OR r.status = :status)")
    Page<Report> findByReporterIdAndStatusOptional(@Param("reporterId") UUID reporterId, @Param("status") ReportStatus status, Pageable pageable);
}