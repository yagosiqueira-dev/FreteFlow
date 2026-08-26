package br.com.freteflow.controller;

import br.com.freteflow.dto.report.BiWeeklyReportDTO;
import br.com.freteflow.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/driver/{driverId}/bi-weekly")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<BiWeeklyReportDTO> getBiWeeklyReport(
            @PathVariable UUID driverId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String driverName) {

        BiWeeklyReportDTO report = reportService.generateBiWeeklyReport(driverId, startDate, endDate, driverName);
        return ResponseEntity.ok(report);
    }
}